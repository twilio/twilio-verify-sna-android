import org.gradle.api.Project

object VersionHelper {

  fun generateVersionCode(project: Project): Int {
    val versionMajor = project.property("versionMajor").toString()
    val versionMinor = project.property("versionMinor").toString()
    val versionPatch = project.property("versionPatch").toString()
    val version = "$versionMajor$versionMinor$versionPatch"
    val code = version.toInt()
    println("Version code: $code")
    return code
  }

  fun generateVersionName(project: Project): String {
    val versionMajor = project.property("versionMajor").toString()
    val versionMinor = project.property("versionMinor").toString()
    val versionPatch = project.property("versionPatch").toString()
    val version = "$versionMajor.$versionMinor.$versionPatch"
    println("Version name: $version")
    return version
  }
}
