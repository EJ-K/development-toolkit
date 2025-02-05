import com.runemate.game.api.bot.data.Category

plugins {
    java
    idea
    id("com.runemate") version "1.5.1"
    id("io.freefair.lombok") version "6.3.0"
}

group = "com.runemate"
version = "1.0.0"

tasks.runClient {
    dependsOn(tasks.build)
}

runemate {
    devMode = true
    debug = true
    autoLogin = true

    // You should not include this line in your own projects, instead see the section on publishing:
    // https://runemate.gitbook.io/runemate-documentation/getting-started/gradle-plugin/publishing
    submissionToken = findProperty("submissionKey.Party").toString()

    manifests {
        create("Development Toolkit") {
            mainClass = "com.runemate.bots.dev.DevelopmentToolkit"
            tagline = "RuneMate's Swiss Army Knife"
            description = "Presents runtime data, such as the loaded NPCs, to assist in bot development."
            version = "2.6.0"
            internalId = "devkit"

            categories(Category.DEVELOPER_TOOLS)
            tags("devkit", "debug")

            resources {
                include("css/DevelopmentToolkitPage.css")
                include("fxml/DevelopmentToolkitPage.fxml")
                include("fxml/QueryBuilderPage.fxml")
            }

            obfuscation {
                exclude("* implements com.runemate.**")
                exclude("com.runemate.bots.**")
            }
        }
    }
}