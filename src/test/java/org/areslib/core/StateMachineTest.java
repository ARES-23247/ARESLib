package org.areslib.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class StateMachineTest {

  enum TestState {
    A,
    B,
    C
  }

  @Test
  void startsInInitialState() {
    StateMachine<TestState> sm = new StateMachine<>(TestState.A);
    assertEquals(TestState.A, sm.getState());
  }

  @Test
  void conditionalTransition() {
    StateMachine<TestState> sm = new StateMachine<>(TestState.A);
    sm.transition(TestState.A, TestState.B, () -> true);
    sm.update();
    assertEquals(TestState.B, sm.getState());
  }

  @Test
  void transitionDoesNotFireWhenFalse() {
    StateMachine<TestState> sm = new StateMachine<>(TestState.A);
    sm.transition(TestState.A, TestState.B, () -> false);
    sm.update();
    assertEquals(TestState.A, sm.getState());
  }

  @Test
  void entryActionRuns() {
    StateMachine<TestState> sm = new StateMachine<>(TestState.A);
    boolean[] ran = {false};
    sm.onEntry(TestState.B, () -> ran[0] = true);
    sm.transition(TestState.A, TestState.B, () -> true);
    sm.update();
    assertTrue(ran[0]);
  }

  @Test
  void exitActionRuns() {
    StateMachine<TestState> sm = new StateMachine<>(TestState.A);
    boolean[] ran = {false};
    sm.onExit(TestState.A, () -> ran[0] = true);
    sm.transition(TestState.A, TestState.B, () -> true);
    sm.update();
    assertTrue(ran[0]);
  }

  @Test
  void duringActionRuns() {
    StateMachine<TestState> sm = new StateMachine<>(TestState.A);
    int[] count = {0};
    sm.during(TestState.A, () -> count[0]++);
    sm.update();
    sm.update();
    sm.update();
    assertEquals(3, count[0]);
  }

  @Test
  void duringActionDoesNotRunInOtherState() {
    StateMachine<TestState> sm = new StateMachine<>(TestState.A);
    int[] count = {0};
    sm.during(TestState.B, () -> count[0]++);
    sm.update();
    assertEquals(0, count[0]);
  }

  @Test
  void previousStateTracked() {
    StateMachine<TestState> sm = new StateMachine<>(TestState.A);
    sm.transition(TestState.A, TestState.B, () -> true);
    sm.update();
    assertEquals(TestState.A, sm.getPreviousState());
    assertEquals(TestState.B, sm.getState());
  }

  @Test
  void forceStateChanges() {
    StateMachine<TestState> sm = new StateMachine<>(TestState.A);
    sm.forceState(TestState.C);
    assertEquals(TestState.C, sm.getState());
  }

  @Test
  void forceStateRunsEntryAndExit() {
    StateMachine<TestState> sm = new StateMachine<>(TestState.A);
    boolean[] exited = {false};
    boolean[] entered = {false};
    sm.onExit(TestState.A, () -> exited[0] = true);
    sm.onEntry(TestState.B, () -> entered[0] = true);
    sm.forceState(TestState.B);
    assertTrue(exited[0]);
    assertTrue(entered[0]);
  }

  @Test
  void isInState() {
    StateMachine<TestState> sm = new StateMachine<>(TestState.A);
    assertTrue(sm.isInState(TestState.A));
    assertFalse(sm.isInState(TestState.B));
  }

  @Test
  void firstMatchWins() {
    StateMachine<TestState> sm = new StateMachine<>(TestState.A);
    sm.transition(TestState.A, TestState.B, () -> true);
    sm.transition(TestState.A, TestState.C, () -> true); // should NOT fire
    sm.update();
    assertEquals(TestState.B, sm.getState());
  }

  @Test
  void chainedTransitions() {
    StateMachine<TestState> sm = new StateMachine<>(TestState.A);
    sm.transition(TestState.A, TestState.B, () -> true);
    sm.transition(TestState.B, TestState.C, () -> true);
    sm.update(); // A -> B
    sm.update(); // B -> C
    assertEquals(TestState.C, sm.getState());
  }

  @Test
  void selfTransitionIgnored() {
    StateMachine<TestState> sm = new StateMachine<>(TestState.A);
    int[] count = {0};
    sm.onEntry(TestState.A, () -> count[0]++);
    sm.forceState(TestState.A); // same state — should be no-op
    assertEquals(0, count[0]);
  }

  // ── Validated Transition Tests ──────────────────────────────────────────────

  @Test
  void validatedTransitionAllowsRegistered() {
    StateMachine<TestState> sm = new StateMachine<>(TestState.A);
    sm.addTransition(TestState.A, TestState.B);

    assertTrue(sm.requestTransition(TestState.B), "Registered transition should be accepted");
    assertEquals(TestState.B, sm.getState());
  }

  @Test
  void validatedTransitionRejectsUnregistered() {
    StateMachine<TestState> sm = new StateMachine<>(TestState.A);
    sm.addTransition(TestState.A, TestState.B);

    assertFalse(
        sm.requestTransition(TestState.C),
        "Unregistered transition should be rejected in validated mode");
    assertEquals(TestState.A, sm.getState(), "State should not change on rejected transition");
  }

  @Test
  void unvalidatedModeAllowsAll() {
    StateMachine<TestState> sm = new StateMachine<>(TestState.A);
    // No transitions registered → unvalidated mode
    assertTrue(sm.isTransitionLegal(TestState.C));
    assertTrue(sm.requestTransition(TestState.C));
    assertEquals(TestState.C, sm.getState());
  }

  @Test
  void bidirectionalTransition() {
    StateMachine<TestState> sm = new StateMachine<>(TestState.A);
    sm.addValidBidirectional(TestState.A, TestState.B);

    assertTrue(sm.requestTransition(TestState.B));
    assertEquals(TestState.B, sm.getState());

    assertTrue(sm.requestTransition(TestState.A));
    assertEquals(TestState.A, sm.getState());
  }

  @Test
  void wildcardToAllowsAllSourceStates() {
    StateMachine<TestState> sm = new StateMachine<>(TestState.A);
    sm.addWildcardTo(TestState.C); // A -> C, B -> C, C -> C (self)

    assertTrue(sm.requestTransition(TestState.C));
    assertEquals(TestState.C, sm.getState());

    sm.forceState(TestState.B);
    assertTrue(sm.requestTransition(TestState.C));
    assertEquals(TestState.C, sm.getState());
  }

  @Test
  void wildcardFromAllowsAllDestinationStates() {
    StateMachine<TestState> sm = new StateMachine<>(TestState.A);
    sm.addWildcardFrom(TestState.A); // A -> A, A -> B, A -> C

    assertTrue(sm.isTransitionLegal(TestState.B));
    assertTrue(sm.isTransitionLegal(TestState.C));
  }

  @Test
  void forceStateBypassesValidation() {
    StateMachine<TestState> sm = new StateMachine<>(TestState.A);
    sm.addTransition(TestState.A, TestState.B); // Only A -> B is legal

    // forceState to C should work despite no registered transition
    sm.forceState(TestState.C);
    assertEquals(TestState.C, sm.getState());
  }

  // ── Graph Emission ─────────────────────────────────────────────────────────

  @Test
  void emitTransitionGraphDoesNotThrow() {
    StateMachine<TestState> sm = new StateMachine<>("TestGraph", TestState.class, TestState.A);
    sm.addTransition(TestState.A, TestState.B);
    sm.addTransition(TestState.B, TestState.C);
    sm.addTransition(TestState.C, TestState.A);

    // Should emit DOT format without throwing
    assertDoesNotThrow(sm::emitTransitionGraph);
  }

  @Test
  void emitTransitionGraphUnvalidated() {
    StateMachine<TestState> sm = new StateMachine<>("Unvalidated", TestState.class, TestState.A);
    // No transitions registered → unvalidated
    assertDoesNotThrow(sm::emitTransitionGraph);
  }

  @Test
  void getValidTransitionsReturnsNullForUnvalidated() {
    StateMachine<TestState> sm = new StateMachine<>(TestState.A);
    assertNull(sm.getValidTransitions());
  }

  @Test
  void getValidTransitionsReturnsMapForValidated() {
    StateMachine<TestState> sm = new StateMachine<>(TestState.A);
    sm.addTransition(TestState.A, TestState.B);

    assertNotNull(sm.getValidTransitions());
    assertTrue(sm.getValidTransitions().containsKey(TestState.A));
    assertTrue(sm.getValidTransitions().get(TestState.A).contains(TestState.B));
  }

  // ── Callback Tests ─────────────────────────────────────────────────────────

  @Test
  void onTransitionCallbackFires() {
    StateMachine<TestState> sm = new StateMachine<>(TestState.A);
    TestState[] captured = new TestState[2];
    sm.setOnTransition(
        (from, to) -> {
          captured[0] = from;
          captured[1] = to;
        });
    sm.transition(TestState.A, TestState.B, () -> true);
    sm.update();
    assertEquals(TestState.A, captured[0]);
    assertEquals(TestState.B, captured[1]);
  }

  // ── Counter / Utility Tests ────────────────────────────────────────────────

  @Test
  void transitionCountIncrementsCorrectly() {
    StateMachine<TestState> sm = new StateMachine<>(TestState.A);
    assertEquals(0, sm.getTotalTransitionCount());

    sm.forceState(TestState.B);
    assertEquals(1, sm.getTotalTransitionCount());

    sm.forceState(TestState.C);
    assertEquals(2, sm.getTotalTransitionCount());
  }

  @Test
  void selfTransitionDoesNotIncrementCount() {
    StateMachine<TestState> sm = new StateMachine<>(TestState.A);
    sm.forceState(TestState.A); // self → no-op
    assertEquals(0, sm.getTotalTransitionCount());
  }

  @Test
  void nameDerivesFromEnumClass() {
    StateMachine<TestState> sm = new StateMachine<>(TestState.A);
    assertEquals("TestState", sm.getName());
  }

  @Test
  void namedConstructorUsesGivenName() {
    StateMachine<TestState> sm = new StateMachine<>("MyMachine", TestState.class, TestState.A);
    assertEquals("MyMachine", sm.getName());
  }
}
