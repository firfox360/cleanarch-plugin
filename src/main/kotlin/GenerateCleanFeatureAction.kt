package com.cleanarch

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import java.nio.charset.StandardCharsets

class GenerateCleanFeatureAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        if (!virtualFile.isDirectory) return

        val rawName = Messages.showInputDialog(
            project,
            "Enter Feature Name:",
            "New Clean Architecture Feature",
            Messages.getQuestionIcon()
        ) ?: return

        if (rawName.isBlank()) return

        val featureName = rawName.trim().lowercase()
        val className = rawName.trim().split("_").joinToString("") { it.capitalize() }

        WriteCommandAction.runWriteCommandAction(project) {
            try {
                val featureDir = virtualFile.createChildDirectory(this, featureName)

                val dataDir = featureDir.createChildDirectory(this, "data")
                val domainDir = featureDir.createChildDirectory(this, "domain")
                val presentationDir = featureDir.createChildDirectory(this, "presentation")

                val datasourcesDir = dataDir.createChildDirectory(this, "datasources")
                dataDir.createChildDirectory(this, "models")
                val dataReposDir = dataDir.createChildDirectory(this, "repositories")

                domainDir.createChildDirectory(this, "entities")
                val domainReposDir = domainDir.createChildDirectory(this, "repositories")
                domainDir.createChildDirectory(this, "usecases")

                val blocDir = presentationDir.createChildDirectory(this, "bloc")
                presentationDir.createChildDirectory(this, "pages")
                presentationDir.createChildDirectory(this, "widgets")

                createFile(domainReposDir, "${featureName}_repository.dart", "abstract class ${className}Repository {\n  // TODO: Add repository methods here\n}\n")
                createFile(dataReposDir, "${featureName}_repository_impl.dart", "import '../../domain/repositories/${featureName}_repository.dart';\n\nclass ${className}RepositoryImpl implements ${className}Repository {\n  // TODO: Implement repository methods here\n}\n")
                createFile(datasourcesDir, "${featureName}_remote_data_source.dart", "abstract class ${className}RemoteDataSource {\n  // TODO: Add remote data source method signatures\n}\n\nclass ${className}RemoteDataSourceImpl implements ${className}RemoteDataSource {\n  // TODO: Implement remote data source methods\n}\n")
                createFile(blocDir, "${featureName}_event.dart", "part of '${featureName}_bloc.dart';\n\nabstract class ${className}Event {}\n")
                createFile(blocDir, "${featureName}_state.dart", "part of '${featureName}_bloc.dart';\n\nabstract class ${className}State {}\n\nclass ${className}Initial extends ${className}State {}\n")
                createFile(blocDir, "${featureName}_bloc.dart", "import 'package:bloc/bloc.dart';\npart '${featureName}_event.dart';\npart '${featureName}_state.dart';\n\nclass ${className}Bloc extends Bloc<${className}Event, ${className}State> {\n  ${className}Bloc() : super(${className}Initial()) {\n    on<${className}Event>((event, emit) {\n      // TODO: implement event handler\n    });\n  }\n}\n")

                virtualFile.refresh(false, true)
            } catch (ex: Exception) {
                Messages.showErrorDialog(project, "Error creating feature: ${ex.message}", "Creation Failed")
            }
        }
    }

    private fun createFile(parent: VirtualFile, fileName: String, content: String) {
        if (parent.findChild(fileName) == null) {
            val file = parent.createChildData(this, fileName)
            file.setBinaryContent(content.toByteArray(StandardCharsets.UTF_8))
        }
    }
}