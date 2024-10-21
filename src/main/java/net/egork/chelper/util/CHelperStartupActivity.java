package net.egork.chelper.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import net.egork.chelper.ChromeParser;
import net.egork.chelper.ProjectData;
import net.egork.chelper.actions.TopCoderAction;
import net.egork.chelper.codegeneration.CodeGenerationUtilities;
import org.jetbrains.annotations.NotNull;

public class CHelperStartupActivity implements StartupActivity {

    @Override
    public void runActivity(@NotNull Project project) {
        ProjectData configuration = ProjectData.load(project);
        if (configuration != null) {
            Utilities.addProjectData(project, configuration);
            TopCoderAction.start(project);
            Utilities.ensureLibrary(project);
            CodeGenerationUtilities.createTaskClassTemplateIfNeeded(project, null);
            CodeGenerationUtilities.createCheckerClassTemplateIfNeeded(project);
            CodeGenerationUtilities.createTestCaseClassTemplateIfNeeded(project);
            CodeGenerationUtilities.createTopCoderTaskTemplateIfNeeded(project);
            CodeGenerationUtilities.createTopCoderTestCaseClassTemplateIfNeeded(project);
            ChromeParser.checkInstalled(project, configuration);
        }
    }
}