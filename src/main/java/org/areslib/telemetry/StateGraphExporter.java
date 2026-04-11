package org.areslib.telemetry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.Map;
import org.areslib.core.StateMachine;

/**
 * Utility to export an ARESLib StateMachine's validated transition table to a Markdown file
 * containing a Mermaid-JS state diagram.
 */
public class StateGraphExporter {

  /**
   * Generates a Mermaid-JS string representing the given StateMachine's valid transitions.
   *
   * @param sm The StateMachine to export.
   * @param <S> The enum class defining the states.
   * @return A string containing a Markdown formatted Mermaid stateDiagram-v2 block.
   */
  public static <S extends Enum<S>> String exportToMermaid(StateMachine<S> sm) {
    Map<S, EnumSet<S>> transitions = sm.getValidTransitions();
    if (transitions == null || transitions.isEmpty()) {
      return "```mermaid\nstateDiagram-v2\n    [*] --> "
          + sm.getName()
          + " : Unvalidated Mode (All Transitions Allowed)\n```";
    }

    StringBuilder md = new StringBuilder();
    md.append("```mermaid\n");
    md.append("stateDiagram-v2\n");
    md.append("    direction TB\n");

    // Initial state indicator could be tricky if not exposed directly, but we just draw the nodes.
    for (Map.Entry<S, EnumSet<S>> entry : transitions.entrySet()) {
      for (S target : entry.getValue()) {
        md.append("    ")
            .append(entry.getKey().name())
            .append(" --> ")
            .append(target.name())
            .append("\n");
      }
    }

    md.append("```\n");
    return md.toString();
  }

  /**
   * Exports the Mermaid-JS state diagram to a local markdown file.
   *
   * @param sm The StateMachine to export.
   * @param outputDir The directory to save the file in.
   * @param <S> The enum class defining the states.
   */
  public static <S extends Enum<S>> void exportToFile(StateMachine<S> sm, String outputDir) {
    File dir = new File(outputDir);
    if (!dir.exists()) {
      dir.mkdirs();
    }

    File file = new File(dir, sm.getName() + "_StateGraph.md");
    try (FileOutputStream fos = new FileOutputStream(file)) {
      String content = "# " + sm.getName() + " State Machine\n\n" + exportToMermaid(sm);
      fos.write(content.getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      com.qualcomm.robotcore.util.RobotLog.e("StateGraphExporter error: " + e.getMessage());
    }
  }
}
