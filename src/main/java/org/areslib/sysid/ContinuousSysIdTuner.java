package org.areslib.sysid;

/**
 * Continuous System Identification Tuner.
 *
 * <p>Performs an online Ordinary Least Squares (OLS) regression using a sliding window to
 * dynamically estimate feedforward constants (kV, kA, kS). Fits the model: V = kV * v + kA * a + kS
 * * sgn(v)
 *
 * <p>Uses a zero-allocation incremental sum technique combined with a fast 3x3 matrix inversion to
 * solve the Normal Equations (X^T X * beta = X^T Y) on the fly.
 */
public class ContinuousSysIdTuner {

  private final int windowSize;
  private int count = 0;
  private int index = 0;

  // Circular buffers for historical data
  private final double[] vBuffer;
  private final double[] aBuffer;
  private final double[] sBuffer; // sgn(v)
  private final double[] volBuffer; // volts

  // Running sums for X^T X
  private double sum_v2 = 0;
  private double sum_va = 0;
  private double sum_vs = 0;
  private double sum_a2 = 0;
  private double sum_as = 0;
  private double sum_s2 = 0;

  // Running sums for X^T Y
  private double sum_vV = 0;
  private double sum_aV = 0;
  private double sum_sV = 0;

  // Computed results
  private double kV = 0;
  private double kA = 0;
  private double kS = 0;

  public ContinuousSysIdTuner(int windowSize) {
    this.windowSize = windowSize;
    vBuffer = new double[windowSize];
    aBuffer = new double[windowSize];
    sBuffer = new double[windowSize];
    volBuffer = new double[windowSize];
  }

  /**
   * Pushes a new sample into the sliding window and updates the OLS model.
   *
   * @param velocity Current mechanism velocity.
   * @param acceleration Current mechanism acceleration.
   * @param volts Applied voltage.
   */
  public void addSample(double velocity, double acceleration, double volts) {
    double signV = Math.signum(velocity);

    // If the buffer is full, subtract the oldest sample's contribution
    if (count == windowSize) {
      double ov = vBuffer[index];
      double oa = aBuffer[index];
      double os = sBuffer[index];
      double ovol = volBuffer[index];

      sum_v2 -= ov * ov;
      sum_va -= ov * oa;
      sum_vs -= ov * os;
      sum_a2 -= oa * oa;
      sum_as -= oa * os;
      sum_s2 -= os * os;

      sum_vV -= ov * ovol;
      sum_aV -= oa * ovol;
      sum_sV -= os * ovol;
    } else {
      count++;
    }

    // Add the new sample's contribution
    sum_v2 += velocity * velocity;
    sum_va += velocity * acceleration;
    sum_vs += velocity * signV;
    sum_a2 += acceleration * acceleration;
    sum_as += acceleration * signV;
    sum_s2 += signV * signV;

    sum_vV += velocity * volts;
    sum_aV += acceleration * volts;
    sum_sV += signV * volts;

    // Store in circular buffer
    vBuffer[index] = velocity;
    aBuffer[index] = acceleration;
    sBuffer[index] = signV;
    volBuffer[index] = volts;

    index = (index + 1) % windowSize;

    // Recompute gains if we have enough data to form a full rank matrix
    if (count > 3) {
      solve();
    }
  }

  /**
   * Solves the 3x3 normal equations iteratively using Cramer's rule / explicit adjugate inversion.
   * X^T X is symmetric: [ sum_v2, sum_va, sum_vs ] [ sum_va, sum_a2, sum_as ] [ sum_vs, sum_as,
   * sum_s2 ]
   */
  private void solve() {
    // X^T X terms (symmetric)
    double m00 = sum_v2, m01 = sum_va, m02 = sum_vs;
    double m10 = sum_va, m11 = sum_a2, m12 = sum_as;
    double m20 = sum_vs, m21 = sum_as, m22 = sum_s2;

    // X^T Y terms
    double y0 = sum_vV;
    double y1 = sum_aV;
    double y2 = sum_sV;

    // Determinant of X^T X
    double det =
        m00 * (m11 * m22 - m12 * m21)
            - m01 * (m10 * m22 - m12 * m20)
            + m02 * (m10 * m21 - m11 * m20);

    // If determinant is near zero, matrix is singular (not enough rich data movement)
    if (Math.abs(det) < 1e-6) {
      return;
    }

    double invDet = 1.0 / det;

    // Inverse matrix computation (adjugate)
    double i00 = (m11 * m22 - m12 * m21) * invDet;
    double i01 = -(m01 * m22 - m02 * m21) * invDet;
    double i02 = (m01 * m12 - m02 * m11) * invDet;

    double i10 = -(m10 * m22 - m12 * m20) * invDet;
    double i11 = (m00 * m22 - m02 * m20) * invDet;
    double i12 = -(m00 * m12 - m02 * m10) * invDet;

    double i20 = (m10 * m21 - m11 * m20) * invDet;
    double i21 = -(m00 * m21 - m01 * m20) * invDet;
    double i22 = (m00 * m11 - m01 * m10) * invDet;

    // Multiply Inverse by X^T Y to get beta [kV, kA, kS]
    kV = i00 * y0 + i01 * y1 + i02 * y2;
    kA = i10 * y0 + i11 * y1 + i12 * y2;
    kS = i20 * y0 + i21 * y1 + i22 * y2;
  }

  public double getKV() {
    return kV;
  }

  public double getKA() {
    return kA;
  }

  public double getKS() {
    return kS;
  }
}
