# GhostVoice — prototype Android

## Ce que contient ce projet
- Application Android native Kotlin.
- Interface sombre minimaliste rouge/noir.
- Activation/désactivation du microphone.
- Traitement audio en direct : assombrissement du timbre (filtre passe-bas) et saturation douce réglable.
- Sortie audio routée par Android vers la sortie actuellement sélectionnée (haut-parleur, casque filaire ou Bluetooth selon l'appareil).

## Limites importantes du prototype
Ce premier prototype est une base fonctionnelle de traitement audio, pas une reproduction de la voix officielle française de Ghostface. Il ne réalise pas encore un véritable pitch-shifting indépendant de la vitesse ni une transformation vocale neuronale. Le rendu sera donc plus sombre et légèrement déformé, mais ne doit pas être présenté comme une imitation réaliste garantie.

Android choisit la route audio active. Une application ordinaire ne peut pas diffuser simultanément et de façon garantie vers tous les périphériques connectés. Pour limiter l'écho et le larsen, utilisez un casque.

## Construire l'APK
1. Installer Android Studio (version récente) et le JDK 17.
2. Ouvrir le dossier `GhostVoice` dans Android Studio.
3. Laisser Gradle synchroniser le projet.
4. Connecter un téléphone Android et autoriser le débogage USB, ou utiliser un émulateur (un vrai téléphone est préférable pour tester le micro).
5. Menu **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
6. L'APK de débogage sera généré dans `app/build/outputs/apk/debug/app-debug.apk`.

## Test
- Accorder l'autorisation microphone.
- Brancher un casque avant d'activer le micro.
- Appuyer sur « ACTIVER LE MICRO » puis parler.
- Régler l'intensité avec le curseur.
- Appuyer sur « DÉSACTIVER LE MICRO » pour arrêter.

## Prochaine étape qualité
Pour obtenir une voix de cinéma plus convaincante, il faudra intégrer un véritable algorithme de pitch-shifting/formant-preserving, optimiser le chemin audio faible latence (AAudio/Oboe via NDK), puis mesurer la latence et tester sur le téléphone cible.

## Générer l'APK automatiquement sans Android Studio
Le projet contient aussi un workflow GitHub Actions (`.github/workflows/build-apk.yml`).
1. Crée un dépôt GitHub et envoie-y le contenu de ce dossier.
2. Ouvre l'onglet **Actions** du dépôt.
3. Sélectionne **Build GhostVoice APK**, puis **Run workflow**.
4. Quand le travail est terminé, ouvre-le et télécharge l'artefact **GhostVoice-debug-apk**.
5. Décompresse l'artefact : il contient `app-debug.apk`.

Cette compilation distante nécessite un compte GitHub et une action manuelle. L'APK n'est pas inclus dans cette archive.
