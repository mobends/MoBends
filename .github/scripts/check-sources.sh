#!/usr/bin/env bash
# Checks the rules of the one-source-tree layout (see CONTRIBUTING.md) that the compiler can't:
#   1. src/ is committed with the vcsVersion of settings.gradle.kts active, so the fenced code is in
#      the state everyone expects and diffs aren't full of comment noise.
#   2. Only goblinbob/mobends/fabric/, neoforge/ and forge/ use a loader API.
# Run from the repository root.
set -euo pipefail

fail=0

active=$(sed -nE 's/^stonecutter active "([^"]+)".*/\1/p' stonecutter.gradle.kts)
vcs=$(sed -nE 's/^[[:space:]]*vcsVersion = "([^"]+)".*/\1/p' settings.gradle.kts)
if [ -z "$active" ] || [ -z "$vcs" ]; then
  echo "::error::Couldn't read the active version (stonecutter.gradle.kts) or vcsVersion (settings.gradle.kts)."
  fail=1
elif [ "$active" != "$vcs" ]; then
  echo "::error file=stonecutter.gradle.kts::'$active' is active, but src/ must be committed with '$vcs' active. Switch to it (Stonecutter's \"Set active project to $vcs\" task) and commit again."
  fail=1
fi

loader_code='goblinbob/mobends/(fabric|neoforge|forge)/'
offenders=$(grep -rlE 'net\.(fabricmc|neoforged|minecraftforge)\.' src/main/java | grep -vE "$loader_code" || true)
for file in $offenders; do
  echo "::error file=$file::Uses a loader API outside goblinbob.mobends.fabric / .neoforge / .forge. Put it behind goblinbob.mobends.platform.Platform."
  fail=1
done

exit $fail
