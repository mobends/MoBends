// The version whose code src/ currently holds. Switch it with the Stonecutter IDE plugin or the
// "Set active project to ..." Gradle tasks, never by hand, and switch back to the vcsVersion of
// settings.gradle.kts before committing (CI checks this).
stonecutter active "1.21.1-fabric"

// See https://stonecutter.kikugie.dev/wiki/config/params
stonecutter parameters {
    val (version, loader) = current.project.split('-', limit = 2)

    // Makes the version- and loader-specific sections of stonecutter.properties.toml apply.
    properties {
        tags(version, loader)
    }

    // `//? if fabric {` and `//? if neoforge {`
    constants {
        match(loader, "fabric", "neoforge")
    }

    swaps["mod_version"] = "\"${properties["mod.version"]}\";"
    swaps["minecraft"] = "\"${current.version}\";"

    replacements {
        string(current.parsed >= "1.21.11") {
            replace("ResourceLocation", "Identifier")
        }

        string(current.parsed >= "26.1") {
            replace("classTweaker v2 named", "classTweaker v2 official")
        }
    }
}
