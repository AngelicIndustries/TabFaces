
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

plugins {
    id("com.gtnewhorizons.gtnhconvention")
}

apply(from = "https://raw.githubusercontent.com/AngelicIndustries/67minecraft-gradle-publish/${property("publish67ScriptTag")}/67minecraft-publish.gradle.kts")

extensions.configure<PublishingExtension> {
    publications.withType<MavenPublication>().configureEach {
        pom {
            name.set("TabFaces")
            description.set("Player faces in the tab menu, chat, and server population tooltip for Minecraft 1.7.10.")
            url.set("https://github.com/AngelicIndustries/TabFaces")
            organization {
                name.set("Angelic Industries")
                url.set("https://angelic.industries/")
            }
            scm {
                url.set("https://github.com/AngelicIndustries/TabFaces")
                connection.set("scm:git:https://github.com/AngelicIndustries/TabFaces.git")
                developerConnection.set("scm:git:ssh://git@github.com/AngelicIndustries/TabFaces.git")
            }
        }
    }
}
