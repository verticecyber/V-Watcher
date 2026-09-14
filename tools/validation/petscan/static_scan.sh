#!/bin/bash
# PET-scan static probes — read-only, reproducible. Run from repo root.
SRC=app/src/main/java/com/example
echo "=== 1. class/fun definitions vs references (candidate orphans) ==="
grep -rhoE "^(class|data class|object|interface|enum class|fun) [A-Za-z0-9_]+" $SRC --include="*.kt" | sort | uniq -c | sort -rn | head -n 60
echo "=== 2. TODO/FIXME/TEMP/HACK/WORKAROUND/XXX with context ==="
grep -rn -E "TODO|FIXME|XXX|HACK|WORKAROUND|TEMP" $SRC --include="*.kt" | head -n 30
echo "=== 3. numeric thresholds (magic numbers in comparisons) ==="
grep -rhoE "(>|<|>=|<=|==|!=) ?[0-9]+(\.[0-9]+)?(f|L|Ms)?" $SRC --include="*.kt" | sort | uniq -c | sort -rn | head -n 40
echo "=== 4. confidence/score/severity fields ==="
grep -rn -E "confidence|Confidence|severity|Severity" $SRC --include="*.kt" | grep -vE "test" | head -n 50
echo "=== 5. payload variants constructed (bus contract usage) ==="
for v in TelemetryArrival PatternDetected RapidResponseRequested AntigenPresented ReasoningDispatched ReasoningConcluded CompromiseSuspected CoordinationDirective AntibodyMatch EffectorActionRequest EffectorActionCompleted CleanupCompleted RegulatorySuppression ResolutionTransition; do printf "%s: " "$v"; grep -rl "$v(" $SRC --include="*.kt" | wc -l; done
echo "=== 6. flow collectors ==="
grep -rn "\.collect" $SRC --include="*.kt" | head -n 20
echo "=== 7. startActivity (real OS transitions) ==="
grep -rn "startActivity" $SRC --include="*.kt" | head -n 10
echo "=== 8. delay/ hardcoded timing ==="
grep -rhoE "delay\([0-9_]+L?\)|_[0-9_]+Ms|Ms = [0-9_]+" $SRC --include="*.kt" | sort | uniq -c | sort -rn | head -n 20
