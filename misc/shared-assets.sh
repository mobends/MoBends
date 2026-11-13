#!/bin/sh
# The assets this branch shares with `main` are main's files, copied as they are at the commit
# `shared_assets_ref` names (gradle.properties). They're never edited here: what 1.8.9 needs
# differently is done in code (core/compat/SharedAssets.java), or asked for upstream on `main`.
#
#   misc/shared-assets.sh check         fails if any shared asset differs from main's at shared_assets_ref
#   misc/shared-assets.sh copy <ref>    copies them from `main` at <ref> and sets shared_assets_ref to it
set -eu

cd "$(dirname "$0")/.."

PATHS="src/main/resources/assets/mobends/bends misc/legacy-pack/assets"

fetch() {
    git fetch --quiet --depth=1 origin "$1"
}

case "${1:-}" in
check)
    ref=$(sed -n 's/^shared_assets_ref=//p' gradle.properties)
    [ -n "$ref" ] || { echo "gradle.properties has no shared_assets_ref."; exit 1; }
    fetch "$ref"
    # Files added here but not on main count too (untracked ones as well).
    untracked=$(git ls-files --others --exclude-standard -- $PATHS)
    if ! git diff --quiet "$ref" -- $PATHS || [ -n "$untracked" ]; then
        git diff --stat "$ref" -- $PATHS
        [ -z "$untracked" ] || printf 'Not on main:\n%s\n' "$untracked"
        echo
        echo "The shared assets differ from main's at $ref (shared_assets_ref)."
        echo "Don't edit them on this branch: make 1.8.9 read them as they are (core/compat/SharedAssets.java),"
        echo "or open an issue on main for what the format needs. To take newer ones from main:"
        echo "misc/shared-assets.sh copy <commit on main>. See CONTRIBUTING.md, Shared assets."
        exit 1
    fi
    echo "The shared assets are main's at $ref."
    ;;
copy)
    ref=${2:?"Usage: misc/shared-assets.sh copy <commit on main>"}
    fetch "$ref"
    sha=$(git rev-parse FETCH_HEAD)
    # Files main removed go too.
    git rm -r -q --ignore-unmatch -- $PATHS
    git checkout "$sha" -- $PATHS
    sed -i.bak "s/^shared_assets_ref=.*/shared_assets_ref=$sha/" gradle.properties && rm gradle.properties.bak
    echo "Copied the shared assets from main at $sha. Run the build and the lab, and check in the game what changed."
    ;;
*)
    sed -n '2,8p' "$0" | sed 's/^# \{0,1\}//'
    exit 1
    ;;
esac
