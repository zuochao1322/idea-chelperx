package net.egork.chelper;

import com.intellij.execution.RunManagerListener;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.vfs.VirtualFile;
import net.egork.chelper.configurations.TaskConfiguration;
import net.egork.chelper.configurations.TopCoderConfiguration;
import net.egork.chelper.task.Task;
import net.egork.chelper.task.TopCoderTask;
import net.egork.chelper.util.FileUtilities;
import net.egork.chelper.util.TaskUtilities;
import net.egork.chelper.util.Utilities;
import org.jetbrains.annotations.NotNull;

/**
 * @author Egor Kulikov (egor@egork.net)
 */
public class AutoSwitcher implements StartupActivity {
    private boolean busy;

    @Override
    public void runActivity(@NotNull Project project) {
        addSelectedConfigurationListener(project);
        addFileEditorListeners(project);
    }

    private void addFileEditorListeners(@NotNull Project project) {
        project.getMessageBus().connect()
                .subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
                    @Override
                    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                        selectTask(file, project);
                    }

                    private void selectTask(final VirtualFile file, final Project project) {
                        Runnable selectTaskRunnable = () -> {
                            if (busy || file == null) {
                                return;
                            }
                            RunManagerImpl runManager = RunManagerImpl.getInstanceImpl(project);
                            for (RunConfiguration configuration : runManager.getAllConfigurationsList()) {
                                if (configuration instanceof TopCoderConfiguration) {
                                    TopCoderTask task = ((TopCoderConfiguration) configuration).getConfiguration();
                                    if (task != null && file.equals(TaskUtilities.getFile(Utilities.getData(project).defaultDirectory, task.name, project))) {
                                        busy = true;
                                        runManager.setSelectedConfiguration(new RunnerAndConfigurationSettingsImpl(runManager,
                                                configuration, false));
                                        busy = false;
                                    }
                                } else if (configuration instanceof TaskConfiguration) {
                                    Task task = ((TaskConfiguration) configuration).getConfiguration();
                                    if (task != null && file.equals(FileUtilities.getFileByFQN(task.taskClass, configuration.getProject()))) {
                                        busy = true;
                                        runManager.setSelectedConfiguration(new RunnerAndConfigurationSettingsImpl(runManager,
                                                configuration, false));
                                        busy = false;
                                        return;
                                    }
                                }
                            }
                        };

                        DumbService.getInstance(project).smartInvokeLater(selectTaskRunnable);
                    }

                    @Override
                    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
                        selectTask(event.getNewFile(), project);
                    }
                });
    }

    private void addSelectedConfigurationListener(@NotNull Project project) {
        project.getMessageBus().connect().subscribe(RunManagerListener.TOPIC, new RunManagerListener() {
            public void selectedConfigurationChanged(@NotNull RunnerAndConfigurationSettings settings) {
                RunConfiguration configuration = settings.getConfiguration();
                if (busy || !(configuration instanceof TopCoderConfiguration || configuration instanceof TaskConfiguration)) {
                    return;
                }
                busy = true;
                VirtualFile toOpen = null;
                if (configuration instanceof TopCoderConfiguration) {
                    toOpen = TaskUtilities.getFile(Utilities.getData(project).defaultDirectory,
                            ((TopCoderConfiguration) configuration).getConfiguration().name, project);
                } else if (configuration instanceof TaskConfiguration) {
                    toOpen = FileUtilities.getFileByFQN(((TaskConfiguration) configuration).getConfiguration().taskClass,
                            configuration.getProject());
                }
                if (toOpen != null) {
                    final VirtualFile finalToOpen = toOpen;
                    ApplicationManager.getApplication().invokeLater(() ->
                            FileEditorManager.getInstance(project).openFile(finalToOpen, true)
                    );
                }
                busy = false;
            }
        });
    }
}