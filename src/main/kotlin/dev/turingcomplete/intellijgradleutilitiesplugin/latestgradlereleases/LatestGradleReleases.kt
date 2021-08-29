package dev.turingcomplete.intellijgradleutilitiesplugin.latestgradlereleases

class LatestGradleReleases(val productive: List<GradleGitHubRelease>,
                           val preRelease: List<GradleGitHubRelease>,
                           val errors: List<String>)