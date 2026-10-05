plugins{
    java
}

// Version de Mindustry contre laquelle on compile (tag de release GitHub).
// Le jeu installé doit être >= minGameVersion déclaré dans mod.hjson.
val mindustryVersion = "v160.5"
val modsDir = File(System.getProperty("user.home"), "Library/Application Support/Mindustry/mods")

group = "minclaude"
version = "0.4.0"

repositories{
    mavenCentral()

    // Télécharge dependencies.jar depuis les releases GitHub de Mindustry (méthode du template officiel).
    ivy{
        url = uri("https://github.com/")
        patternLayout{
            // [classifier].jar en premier : "Anuken:Mindustry:vX:assets" -> assets.jar ; sans classifier -> dependencies.jar
            artifact("/[organisation]/[module]/releases/download/[revision]/[classifier].jar")
            artifact("/[organisation]/[module]/releases/download/[revision]/dependencies(-[classifier]).jar")
        }
        metadataSources{ artifact() }
        content{ includeVersion("Anuken", "Mindustry", mindustryVersion) }
    }
}

java{
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

sourceSets{
    // Outils hors jeu (générateur de sprites). Jamais inclus dans le jar du mod.
    create("tools")
    // Tests d'intégration : démarrent Mindustry en headless avec le contenu du mod.
    create("integrationTest"){
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

val integrationTestImplementation by configurations.getting{ extendsFrom(configurations.testImplementation.get()) }
val integrationTestRuntimeOnly by configurations.getting{ extendsFrom(configurations.testRuntimeOnly.get()) }
val gameAssets by configurations.creating

val mindustry = "Anuken:Mindustry:$mindustryVersion"

dependencies{
    compileOnly(mindustry)

    testImplementation(mindustry)
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Fichiers internes du jeu (cartes, shaders…) nécessaires à Vars.init() en headless.
    gameAssets("$mindustry:assets")
}

tasks.withType<JavaCompile>().configureEach{
    options.encoding = "UTF-8"
    options.release.set(17)
}

tasks.test{
    useJUnitPlatform()
    // Les tests vérifient aussi les assets (bundles, sprites, mod.hjson) à la racine du projet.
    workingDir = projectDir
    testLogging{
        events("failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

val unpackGameAssets = tasks.register<Sync>("unpackGameAssets"){
    from(gameAssets.elements.map{ els -> els.map{ zipTree(it) } })
    into(layout.buildDirectory.dir("game-assets"))
}

val integrationTest = tasks.register<Test>("integrationTest"){
    group = "verification"
    description = "Démarre Mindustry en headless et vérifie le contenu, la sauvegarde et le suivi des ressources."
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    dependsOn(unpackGameAssets)
    useJUnitPlatform()
    // Vars est statique : un JVM par classe de test.
    forkEvery = 1
    workingDir = layout.buildDirectory.dir("game-assets/assets").get().asFile
    systemProperty("minclaude.projectDir", projectDir.absolutePath)
    shouldRunAfter(tasks.test)
    testLogging{
        events("failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

tasks.check{ dependsOn(integrationTest) }

val generateSprites = tasks.register<JavaExec>("generateSprites"){
    group = "minclaude"
    description = "Génère les sprites pixel art dans assets/sprites."
    classpath = sourceSets["tools"].runtimeClasspath
    mainClass.set("minclaude.tools.SpriteGenerator")
    args(file("assets/sprites").absolutePath, file("icon.png").absolutePath)
}

tasks.jar{
    archiveFileName.set("MinClaude.jar")
    from(projectDir){
        include("mod.hjson")
        include("icon.png")
    }
    from("assets/"){
        include("**")
    }
}

tasks.register<Copy>("deployLocal"){
    group = "minclaude"
    description = "Construit le jar, lance les tests et le copie dans le dossier mods de Mindustry (macOS)."
    dependsOn(tasks.check)
    from(tasks.jar)
    into(modsDir)
}

tasks.register<Exec>("play"){
    group = "minclaude"
    description = "Déploie le mod puis lance Mindustry."
    dependsOn("deployLocal")
    commandLine("open", "-a", "Mindustry")
}

// Recette automatique de l'interface dans le vrai client (rendu réel), dans un dossier de données isolé :
// les sauvegardes, options et autres mods du joueur ne sont pas touchés. Captures et rapport dans build/selftest/out.
tasks.register<Exec>("selfTest"){
    group = "verification"
    description = "Lance Mindustry avec MinClaude seul, parcourt l'interface, enregistre captures et rapport, puis quitte."
    dependsOn(tasks.jar)
    val game = File("/Applications/Mindustry.app/Contents/Resources")
    val root = layout.buildDirectory.dir("selftest").get().asFile
    workingDir = game
    doFirst{
        delete(root)
        File(root, "data/mods").mkdirs()
        File(root, "out").mkdirs()
        copy{
            from(tasks.jar)
            into(File(root, "data/mods"))
        }
    }
    environment("MINCLAUDE_SELFTEST", File(root, "out").absolutePath)
    commandLine(
        File(game, "jre/bin/java").absolutePath, "-XstartOnFirstThread", "-XX:+UseCompactObjectHeaders",
        "--enable-native-access=ALL-UNNAMED", "-Dmindustry.data.dir=" + File(root, "data").absolutePath,
        "-jar", "desktop.jar"
    )
    doLast{
        val report = File(root, "out/report.txt")
        println(report.readText())
        if(!report.readText().contains("RÉSULTAT : OK")) throw GradleException("Autotest en jeu en échec, voir $report")
    }
}

tasks.register<JavaExec>("spriteSheet"){
    group = "minclaude"
    description = "Assemble tous les sprites dans docs/images/sprites.png (relecture et documentation)."
    classpath = sourceSets["tools"].runtimeClasspath
    mainClass.set("minclaude.tools.SpriteSheet")
    args(file("assets/sprites").absolutePath, file("docs/images/sprites.png").absolutePath)
}
