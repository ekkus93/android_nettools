package dev.nettools.android.data.workspace

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.nettools.android.data.curl.CurlDefaults
import dev.nettools.android.domain.model.WorkspaceEntry
import dev.nettools.android.domain.repository.CurlSettingsRepository
import dev.nettools.android.domain.repository.WorkspaceRepository
import java.io.File
import java.io.InputStream
import java.io.IOException
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Filesystem-backed implementation of the curl workspace model.
 */
@Singleton
class WorkspaceManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val settingsRepository: CurlSettingsRepository,
) : WorkspaceRepository {

    override suspend fun getWorkspaceRootPath(): String {
        val configuredRoot = settingsRepository.getSettings().workspaceRootPath
        val root = if (configuredRoot.isNullOrBlank()) {
            File(context.filesDir, CurlDefaults.defaultWorkspaceDirectoryName)
        } else {
            File(configuredRoot)
        }
        if (!root.exists()) {
            root.mkdirs()
        }
        return root.absolutePath
    }

    override suspend fun list(path: String): List<WorkspaceEntry> {
        val directory = resolveToFile(path)
        if (!directory.exists()) {
            throw IOException("Workspace path does not exist: ${normalizePath(path)}")
        }
        if (!directory.isDirectory) {
            throw IOException("Workspace path is not a directory: ${normalizePath(path)}")
        }
        return directory.listFiles()
            ?.sortedWith(compareBy<File>({ !it.isDirectory }, { it.name.lowercase() }))
            ?.map { file -> file.toWorkspaceEntry(rootFile = workspaceRootFile(), path = pathOf(file)) }
            ?: emptyList()
    }

    override suspend fun createDirectory(path: String) {
        val directory = resolveToFile(path)
        if (directory.exists()) {
            throw IOException("Workspace entry already exists: ${normalizePath(path)}")
        }
        if (!directory.mkdirs()) {
            throw IOException("Unable to create workspace directory: ${normalizePath(path)}")
        }
    }

    override suspend fun rename(path: String, newName: String): WorkspaceEntry {
        require(newName.isNotBlank()) { "New name must not be blank." }
        val source = resolveToFile(path)
        if (!source.exists()) {
            throw IOException("Workspace entry does not exist: ${normalizePath(path)}")
        }
        val target = File(source.parentFile ?: workspaceRootFile(), newName)
        if (target.exists()) {
            throw IOException("Workspace entry already exists: ${pathOf(target)}")
        }
        if (!source.renameTo(target)) {
            throw IOException("Unable to rename workspace entry: ${normalizePath(path)}")
        }
        return target.toWorkspaceEntry(rootFile = workspaceRootFile(), path = pathOf(target))
    }

    override suspend fun move(path: String, destinationDirectoryPath: String): WorkspaceEntry {
        val source = resolveToFile(path)
        val destinationDirectory = resolveToFile(destinationDirectoryPath)
        if (!source.exists()) {
            throw IOException("Workspace entry does not exist: ${normalizePath(path)}")
        }
        if (!destinationDirectory.exists() || !destinationDirectory.isDirectory) {
            throw IOException("Destination is not a workspace directory: ${normalizePath(destinationDirectoryPath)}")
        }
        val target = File(destinationDirectory, source.name)
        if (target.exists()) {
            throw IOException("Workspace entry already exists: ${pathOf(target)}")
        }
        if (!source.renameTo(target)) {
            throw IOException("Unable to move workspace entry: ${normalizePath(path)}")
        }
        return target.toWorkspaceEntry(rootFile = workspaceRootFile(), path = pathOf(target))
    }

    override suspend fun delete(path: String) {
        val target = resolveToFile(path)
        if (!target.exists()) return
        if (!target.deleteRecursively()) {
            throw IOException("Unable to delete workspace entry: ${normalizePath(path)}")
        }
    }

    override fun normalizePath(path: String): String = WorkspacePathResolver.normalize(path)

    override suspend fun resolveLocalPath(path: String): String = resolveToFile(path).absolutePath

    override suspend fun writeTextFile(path: String, text: String): WorkspaceEntry {
        val target = resolveToFile(path)
        target.parentFile?.mkdirs()
        target.writeText(text)
        return target.toWorkspaceEntry(rootFile = workspaceRootFile(), path = pathOf(target))
    }

    override suspend fun importFile(targetDirectoryPath: String, fileName: String, inputStream: InputStream): WorkspaceEntry {
        val directory = resolveToFile(targetDirectoryPath)
        if (!directory.exists() || !directory.isDirectory) {
            throw IOException("Destination is not a workspace directory: ${normalizePath(targetDirectoryPath)}")
        }
        val sanitizedFileName = File(fileName).name.trim()
        require(sanitizedFileName.isNotEmpty()) { "Imported file name must not be blank." }
        val target = File(directory, sanitizedFileName)
        if (target.exists()) {
            throw IOException("Workspace entry already exists: ${pathOf(target)}")
        }
        inputStream.use { input ->
            target.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return target.toWorkspaceEntry(rootFile = workspaceRootFile(), path = pathOf(target))
    }

    override suspend fun exportFile(path: String, outputStream: OutputStream) {
        val source = resolveToFile(path)
        if (!source.exists() || !source.isFile) {
            throw IOException("Workspace file does not exist: ${normalizePath(path)}")
        }
        outputStream.use { output ->
            source.inputStream().use { input ->
                input.copyTo(output)
            }
        }
    }

    private suspend fun resolveToFile(path: String): File {
        val root = workspaceRootFile()
        val normalized = normalizePath(path).removePrefix("/")
        return if (normalized.isEmpty()) root else File(root, normalized)
    }

    private suspend fun workspaceRootFile(): File = File(getWorkspaceRootPath())

    private suspend fun pathOf(file: File): String {
        val root = workspaceRootFile()
        val relative = file.relativeTo(root).invariantSeparatorsPath
        return if (relative.isEmpty()) "/" else "/$relative"
    }
}

private fun File.toWorkspaceEntry(rootFile: File, path: String): WorkspaceEntry = WorkspaceEntry(
    path = path,
    name = if (this == rootFile) "/" else name,
    isDirectory = isDirectory,
    sizeBytes = if (isDirectory) 0L else length(),
    modifiedAt = lastModified(),
)
