/*******************************************************************************
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ui.internal.ide.handlers;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * Creates a Java 21 Gradle project scaffold for agent-driven workflows.
 */
public class CreateJava21GradleProjectHandler extends AbstractHandler {

	public static final String COMMAND_ID = "org.eclipse.ui.project.createJava21GradleProject"; //$NON-NLS-1$
	public static final String COMMAND_ID_ALIAS = "org.eclipse.ui.project.createProjectFromPrompt"; //$NON-NLS-1$
	public static final String COMMAND_ID_MAVEN = "org.eclipse.ui.project.createMavenProjectFromPrompt"; //$NON-NLS-1$
	public static final String PARAM_PROJECT_NAME = "projectName"; //$NON-NLS-1$
	public static final String PARAM_BASE_PACKAGE = "basePackage"; //$NON-NLS-1$
	public static final String PARAM_PROMPT = "prompt"; //$NON-NLS-1$
	public static final String PARAM_RUN_BUILD = "runBuild"; //$NON-NLS-1$
	public static final String PARAM_BUILD_TOOL = "buildTool"; //$NON-NLS-1$
	private static final String DEFAULT_PROJECT_NAME = "Java21GradleProject"; //$NON-NLS-1$
	private static final String DEFAULT_BASE_PACKAGE = "com.example.demo"; //$NON-NLS-1$
	private static final DateTimeFormatter PROJECT_SUFFIX_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss"); //$NON-NLS-1$
	private static final Pattern PROJECT_NAME_PATTERN = Pattern.compile(
			"(?i)(?:project\\s*name|프로젝트\\s*이름)\\s*(?:is|=|:|은|는)?\\s*([A-Za-z0-9._-]+)"); //$NON-NLS-1$
	private static final Pattern PROJECT_NAMED_PATTERN = Pattern.compile(
			"(?i)(?:named|이름[은는]?|프로젝트명[은는]?|name[은는]?)\\s*([A-Za-z0-9._-]+)"); //$NON-NLS-1$
	private static final Pattern PROJECT_QUOTED_PATTERN = Pattern.compile("\"([A-Za-z0-9._-]{2,})\""); //$NON-NLS-1$
	private static final Pattern PACKAGE_PATTERN = Pattern.compile(
			"(?i)(?:base\\s*package|package|패키지)\\s*(?:is|=|:|은|는)?\\s*([a-zA-Z][a-zA-Z0-9_.]*)"); //$NON-NLS-1$

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String prompt = event.getParameter(PARAM_PROMPT);
		String projectName = firstNonBlank(event.getParameter(PARAM_PROJECT_NAME), extractProjectName(prompt));
		String basePackage = firstNonBlank(event.getParameter(PARAM_BASE_PACKAGE), extractBasePackage(prompt));
		boolean runBuild = Boolean.parseBoolean(firstNonBlank(event.getParameter(PARAM_RUN_BUILD), "false")); //$NON-NLS-1$
		String explicitBuildTool = firstNonBlank(event.getParameter(PARAM_BUILD_TOOL), null);
		BuildTool buildTool = detectBuildTool(explicitBuildTool, prompt);
		boolean springBoot = shouldUseSpringBoot(prompt);
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		projectName = resolveProjectName(workspace, projectName);
		basePackage = normalizePackageName(basePackage, projectName);

		try {
			final String finalProjectName = projectName;
			final String finalBasePackage = basePackage;
			workspace.run(
					monitor -> createScaffold(workspace, finalProjectName, finalBasePackage, runBuild, buildTool, springBoot),
					null, IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
		} catch (CoreException e) {
			IDEWorkbenchPlugin.log("Failed to create Java 21 Gradle project scaffold", e); //$NON-NLS-1$
			throw new ExecutionException("Failed to create Java 21 Gradle project scaffold", e); //$NON-NLS-1$
		}
		return Status.OK_STATUS;
	}

	private void createScaffold(IWorkspace workspace, String projectName, String basePackage, boolean runBuild,
			BuildTool buildTool, boolean springBoot) throws CoreException {
		IProgressMonitor monitor = new NullProgressMonitor();
		IProject project = workspace.getRoot().getProject(projectName);

		IProjectDescription description = workspace.newProjectDescription(projectName);
		project.create(description, monitor);
		project.open(monitor);

		IFolder srcMainJava = project.getFolder("src/main/java"); //$NON-NLS-1$
		IFolder srcTestJava = project.getFolder("src/test/java"); //$NON-NLS-1$
		IFolder packageMainFolder = srcMainJava.getFolder(basePackage.replace('.', '/'));
		IFolder packageTestFolder = srcTestJava.getFolder(basePackage.replace('.', '/'));

		createFolderRecursively(packageMainFolder, monitor);
		createFolderRecursively(packageTestFolder, monitor);

		if (buildTool == BuildTool.MAVEN) {
			createFile(project.getFile("pom.xml"), pomXml(projectName, basePackage, springBoot), monitor); //$NON-NLS-1$
		} else {
			createFile(project.getFile("settings.gradle"), settingsGradle(projectName), monitor); //$NON-NLS-1$
			createFile(project.getFile("build.gradle"), buildGradle(basePackage, springBoot), monitor); //$NON-NLS-1$
			createFile(project.getFile("gradle.properties"), gradleProperties(), monitor); //$NON-NLS-1$
		}
		createFile(project.getFile(".gitignore"), gitIgnore(), monitor); //$NON-NLS-1$
		createFile(project.getFile("README.md"), readme(projectName), monitor); //$NON-NLS-1$
		createFile(packageMainFolder.getFile(mainClassFileName(springBoot, projectName)),
				appJava(basePackage, springBoot, projectName), monitor);
		createFile(packageTestFolder.getFile(mainTestFileName(springBoot, projectName)),
				appTestJava(basePackage, springBoot, projectName),
				monitor);

		if (springBoot) {
			IFolder resources = project.getFolder("src/main/resources"); //$NON-NLS-1$
			createFolderRecursively(resources, monitor);
			createFile(resources.getFile("application.properties"), springApplicationProperties(), monitor); //$NON-NLS-1$

			IFolder controllerFolder = srcMainJava.getFolder(basePackage.replace('.', '/') + "/controller"); //$NON-NLS-1$
			IFolder serviceFolder = srcMainJava.getFolder(basePackage.replace('.', '/') + "/service"); //$NON-NLS-1$
			IFolder entityFolder = srcMainJava.getFolder(basePackage.replace('.', '/') + "/entity"); //$NON-NLS-1$
			IFolder repositoryFolder = srcMainJava.getFolder(basePackage.replace('.', '/') + "/repository"); //$NON-NLS-1$

			createFolderRecursively(controllerFolder, monitor);
			createFolderRecursively(serviceFolder, monitor);
			createFolderRecursively(entityFolder, monitor);
			createFolderRecursively(repositoryFolder, monitor);

			createFile(controllerFolder.getFile("HelloController.java"), helloControllerJava(basePackage), monitor); //$NON-NLS-1$
			createFile(entityFolder.getFile("MemberEntity.java"), memberEntityJava(basePackage), monitor); //$NON-NLS-1$
			createFile(repositoryFolder.getFile("MemberRepository.java"), memberRepositoryJava(basePackage), monitor); //$NON-NLS-1$
			createFile(serviceFolder.getFile("MemberService.java"), memberServiceJava(basePackage), monitor); //$NON-NLS-1$
		}

		project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		if (runBuild) {
			workspace.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
		}
	}

	private static void createFolderRecursively(IFolder folder, IProgressMonitor monitor) throws CoreException {
		if (folder.exists()) {
			return;
		}
		if (folder.getParent() instanceof IFolder parentFolder) {
			createFolderRecursively(parentFolder, monitor);
		}
		folder.create(true, true, monitor);
	}

	private static void createFile(IFile file, String content, IProgressMonitor monitor) throws CoreException {
		try (ByteArrayInputStream source = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
			file.create(source, true, monitor);
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH,
					"Failed to create file: " + file.getProjectRelativePath(), e)); //$NON-NLS-1$
		}
	}

	private static String resolveProjectName(IWorkspace workspace, String value) {
		String normalized = normalizeProjectName(workspace, value);
		IProject existingProject = workspace.getRoot().getProject(normalized);
		if (!existingProject.exists()) {
			return normalized;
		}
		String suffix = LocalDateTime.now().format(PROJECT_SUFFIX_FORMAT);
		String candidate = normalized + "-" + suffix; //$NON-NLS-1$
		IStatus validation = workspace.validateName(candidate, IResource.PROJECT);
		if (validation.isOK() && !workspace.getRoot().getProject(candidate).exists()) {
			return candidate;
		}
		for (int i = 1; i <= 99; i++) {
			String indexedCandidate = normalized + "-" + i; //$NON-NLS-1$
			IStatus indexedValidation = workspace.validateName(indexedCandidate, IResource.PROJECT);
			if (indexedValidation.isOK() && !workspace.getRoot().getProject(indexedCandidate).exists()) {
				return indexedCandidate;
			}
		}
		return DEFAULT_PROJECT_NAME + "-" + suffix; //$NON-NLS-1$
	}

	private static String normalizeProjectName(IWorkspace workspace, String value) {
		if (value == null || value.isBlank()) {
			return DEFAULT_PROJECT_NAME;
		}
		String trimmed = value.trim().replace(' ', '-');
		IStatus validation = workspace.validateName(trimmed, IResource.PROJECT);
		if (!validation.isOK()) {
			return DEFAULT_PROJECT_NAME;
		}
		return trimmed;
	}

	private static String normalizePackageName(String value, String projectName) {
		if (value == null || value.isBlank()) {
			return packageFromProjectName(projectName);
		}
		String trimmed = value.trim();
		if (!trimmed.matches("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)*")) { //$NON-NLS-1$
			return packageFromProjectName(projectName);
		}
		return trimmed;
	}

	private static String firstNonBlank(String preferred, String fallback) {
		if (preferred != null && !preferred.isBlank()) {
			return preferred;
		}
		return fallback;
	}

	private static String extractProjectName(String prompt) {
		String explicit = extractByPattern(prompt, PROJECT_NAME_PATTERN);
		if (explicit != null) {
			return explicit;
		}
		String named = extractByPattern(prompt, PROJECT_NAMED_PATTERN);
		if (named != null) {
			return named;
		}
		return extractByPattern(prompt, PROJECT_QUOTED_PATTERN);
	}

	private static String extractBasePackage(String prompt) {
		return extractByPattern(prompt, PACKAGE_PATTERN);
	}

	private static String extractByPattern(String input, Pattern pattern) {
		if (input == null || input.isBlank()) {
			return null;
		}
		Matcher matcher = pattern.matcher(input);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}

	private static BuildTool detectBuildTool(String explicitBuildTool, String prompt) {
		if (explicitBuildTool != null) {
			if ("maven".equalsIgnoreCase(explicitBuildTool)) { //$NON-NLS-1$
				return BuildTool.MAVEN;
			}
			if ("gradle".equalsIgnoreCase(explicitBuildTool)) { //$NON-NLS-1$
				return BuildTool.GRADLE;
			}
		}
		if (prompt != null) {
			String lower = prompt.toLowerCase();
			if (lower.contains("maven") || lower.contains("pom.xml")) { //$NON-NLS-1$ //$NON-NLS-2$
				return BuildTool.MAVEN;
			}
		}
		return BuildTool.GRADLE;
	}

	private static boolean shouldUseSpringBoot(String prompt) {
		if (prompt == null || prompt.isBlank()) {
			return false;
		}
		String lower = prompt.toLowerCase();
		return lower.contains("spring boot") || lower.contains("spring") || lower.contains("rest api"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	private static String settingsGradle(String projectName) {
		return "rootProject.name = '" + projectName + "'\n"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static String packageFromProjectName(String projectName) {
		if (projectName == null || projectName.isBlank()) {
			return DEFAULT_BASE_PACKAGE;
		}
		String candidate = projectName.toLowerCase().replaceAll("[^a-z0-9]+", "."); //$NON-NLS-1$ //$NON-NLS-2$
		candidate = candidate.replaceAll("\\.+", "."); //$NON-NLS-1$ //$NON-NLS-2$
		candidate = candidate.replaceAll("^\\.|\\.$", ""); //$NON-NLS-1$ //$NON-NLS-2$
		if (candidate.isBlank()) {
			return DEFAULT_BASE_PACKAGE;
		}
		if (!Character.isLetter(candidate.charAt(0))) {
			candidate = "app." + candidate; //$NON-NLS-1$
		}
		return "com.example." + candidate; //$NON-NLS-1$
	}

	private static String buildGradle(String basePackage, boolean springBoot) {
		if (springBoot) {
			String template = """
					plugins {
					    id 'java'
					    id 'org.springframework.boot' version '3.5.0'
					    id 'io.spring.dependency-management' version '1.1.7'
					}

					group = 'com.example'
					version = '0.1.0'

					java {
					    toolchain {
					        languageVersion = JavaLanguageVersion.of(21)
					    }
					}

					repositories {
					    mavenCentral()
					}

					dependencies {
					    implementation 'org.springframework.boot:spring-boot-starter-web'
					    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
					    runtimeOnly 'com.h2database:h2'
					    testImplementation 'org.springframework.boot:spring-boot-starter-test'
					}

					tasks.test {
					    useJUnitPlatform()
					}
					"""; //$NON-NLS-1$
			return template;
		}
		String template = """
				plugins {
				    id 'java'
				    id 'application'
				}

				group = 'com.example'
				version = '0.1.0'

				java {
				    toolchain {
				        languageVersion = JavaLanguageVersion.of(21)
				    }
				}

				repositories {
				    mavenCentral()
				}

				dependencies {
				    testImplementation 'org.junit.jupiter:junit-jupiter:5.11.4'
				}

				application {
				    mainClass = '%s.App'
				}

				tasks.test {
				    useJUnitPlatform()
				}
				"""; //$NON-NLS-1$
		return template.formatted(basePackage);
	}

	private static String pomXml(String projectName, String basePackage, boolean springBoot) {
		if (!springBoot) {
			return """
					<project xmlns="http://maven.apache.org/POM/4.0.0"
					         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
					         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
					  <modelVersion>4.0.0</modelVersion>
					  <groupId>com.example</groupId>
					  <artifactId>%s</artifactId>
					  <version>0.1.0</version>
					  <properties>
					    <maven.compiler.release>21</maven.compiler.release>
					    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
					  </properties>
					  <dependencies>
					    <dependency>
					      <groupId>org.junit.jupiter</groupId>
					      <artifactId>junit-jupiter</artifactId>
					      <version>5.11.4</version>
					      <scope>test</scope>
					    </dependency>
					  </dependencies>
					  <build>
					    <plugins>
					      <plugin>
					        <groupId>org.apache.maven.plugins</groupId>
					        <artifactId>maven-surefire-plugin</artifactId>
					        <version>3.5.2</version>
					      </plugin>
					    </plugins>
					  </build>
					</project>
					""".formatted(projectName); //$NON-NLS-1$
		}
		return """
				<project xmlns="http://maven.apache.org/POM/4.0.0"
				         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
				         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
				  <modelVersion>4.0.0</modelVersion>
				  <parent>
				    <groupId>org.springframework.boot</groupId>
				    <artifactId>spring-boot-starter-parent</artifactId>
				    <version>3.5.0</version>
				    <relativePath/>
				  </parent>
				  <groupId>com.example</groupId>
				  <artifactId>%s</artifactId>
				  <version>0.1.0</version>
				  <name>%s</name>
				  <properties>
				    <java.version>21</java.version>
				  </properties>
				  <dependencies>
				    <dependency>
				      <groupId>org.springframework.boot</groupId>
				      <artifactId>spring-boot-starter-web</artifactId>
				    </dependency>
				    <dependency>
				      <groupId>org.springframework.boot</groupId>
				      <artifactId>spring-boot-starter-data-jpa</artifactId>
				    </dependency>
				    <dependency>
				      <groupId>com.h2database</groupId>
				      <artifactId>h2</artifactId>
				      <scope>runtime</scope>
				    </dependency>
				    <dependency>
				      <groupId>org.springframework.boot</groupId>
				      <artifactId>spring-boot-starter-test</artifactId>
				      <scope>test</scope>
				    </dependency>
				  </dependencies>
				  <build>
				    <plugins>
				      <plugin>
				        <groupId>org.springframework.boot</groupId>
				        <artifactId>spring-boot-maven-plugin</artifactId>
				      </plugin>
				    </plugins>
				  </build>
				</project>
				""".formatted(projectName, projectName); //$NON-NLS-1$
	}

	private static String readme(String projectName) {
		return "# " + projectName + "\n\nGenerated by Eclipse IDE agent scaffold command.\n"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static String gradleProperties() {
		return "org.gradle.jvmargs=-Xmx1g -Dfile.encoding=UTF-8\n"; //$NON-NLS-1$
	}

	private static String gitIgnore() {
		return """
				.gradle/
				build/
				!gradle/wrapper/gradle-wrapper.jar
				.classpath
				.project
				.settings/
				*.iml
				out/
				bin/
				"""; //$NON-NLS-1$
	}

	private static String appJava(String basePackage, boolean springBoot, String projectName) {
		if (springBoot) {
			return "package " + basePackage + ";\n\n" //$NON-NLS-1$ //$NON-NLS-2$
					+ "import org.springframework.boot.SpringApplication;\n" //$NON-NLS-1$
					+ "import org.springframework.boot.autoconfigure.SpringBootApplication;\n\n" //$NON-NLS-1$
					+ "@SpringBootApplication\n" //$NON-NLS-1$
					+ "public class " + mainClassName(projectName) + " {\n" //$NON-NLS-1$ //$NON-NLS-2$
					+ "    public static void main(String[] args) {\n" //$NON-NLS-1$
					+ "        SpringApplication.run(" + mainClassName(projectName) + ".class, args);\n" //$NON-NLS-1$ //$NON-NLS-2$
					+ "    }\n" //$NON-NLS-1$
					+ "}\n"; //$NON-NLS-1$
		}
		return "package " + basePackage + ";\n\n" //$NON-NLS-1$ //$NON-NLS-2$
				+ "public class App {\n" //$NON-NLS-1$
				+ "    public static void main(String[] args) {\n" //$NON-NLS-1$
				+ "        System.out.println(\"Hello from Java 21 Gradle project\");\n" //$NON-NLS-1$
				+ "    }\n" //$NON-NLS-1$
				+ "}\n"; //$NON-NLS-1$
	}

	private static String appTestJava(String basePackage, boolean springBoot, String projectName) {
		if (springBoot) {
			return "package " + basePackage + ";\n\n" //$NON-NLS-1$ //$NON-NLS-2$
					+ "import org.junit.jupiter.api.Test;\n" //$NON-NLS-1$
					+ "import org.springframework.boot.test.context.SpringBootTest;\n\n" //$NON-NLS-1$
					+ "@SpringBootTest\n" //$NON-NLS-1$
					+ "class " + mainClassName(projectName) + "Tests {\n" //$NON-NLS-1$ //$NON-NLS-2$
					+ "    @Test\n" //$NON-NLS-1$
					+ "    void contextLoads() {\n" //$NON-NLS-1$
					+ "    }\n" //$NON-NLS-1$
					+ "}\n"; //$NON-NLS-1$
		}
		return "package " + basePackage + ";\n\n" //$NON-NLS-1$ //$NON-NLS-2$
				+ "import static org.junit.jupiter.api.Assertions.assertTrue;\n\n" //$NON-NLS-1$
				+ "import org.junit.jupiter.api.Test;\n\n" //$NON-NLS-1$
				+ "class AppTest {\n" //$NON-NLS-1$
				+ "    @Test\n" //$NON-NLS-1$
				+ "    void sanity() {\n" //$NON-NLS-1$
				+ "        assertTrue(true);\n" //$NON-NLS-1$
				+ "    }\n" //$NON-NLS-1$
				+ "}\n"; //$NON-NLS-1$
	}

	private static String springApplicationProperties() {
		return """
				server.port=8080
				spring.datasource.url=jdbc:h2:mem:testdb
				spring.datasource.driverClassName=org.h2.Driver
				spring.datasource.username=sa
				spring.datasource.password=
				spring.jpa.hibernate.ddl-auto=update
				spring.h2.console.enabled=true
				"""; //$NON-NLS-1$
	}

	private static String helloControllerJava(String basePackage) {
		return "package " + basePackage + ".controller;\n\n" //$NON-NLS-1$ //$NON-NLS-2$
				+ "import org.springframework.web.bind.annotation.GetMapping;\n" //$NON-NLS-1$
				+ "import org.springframework.web.bind.annotation.RequestMapping;\n" //$NON-NLS-1$
				+ "import org.springframework.web.bind.annotation.RestController;\n\n" //$NON-NLS-1$
				+ "@RestController\n" //$NON-NLS-1$
				+ "@RequestMapping(\"/api\")\n" //$NON-NLS-1$
				+ "public class HelloController {\n" //$NON-NLS-1$
				+ "    @GetMapping(\"/hello\")\n" //$NON-NLS-1$
				+ "    public String hello() {\n" //$NON-NLS-1$
				+ "        return \"hello\";\n" //$NON-NLS-1$
				+ "    }\n" //$NON-NLS-1$
				+ "}\n"; //$NON-NLS-1$
	}

	private static String memberEntityJava(String basePackage) {
		return "package " + basePackage + ".entity;\n\n" //$NON-NLS-1$ //$NON-NLS-2$
				+ "import jakarta.persistence.Entity;\n" //$NON-NLS-1$
				+ "import jakarta.persistence.GeneratedValue;\n" //$NON-NLS-1$
				+ "import jakarta.persistence.GenerationType;\n" //$NON-NLS-1$
				+ "import jakarta.persistence.Id;\n\n" //$NON-NLS-1$
				+ "@Entity\n" //$NON-NLS-1$
				+ "public class MemberEntity {\n" //$NON-NLS-1$
				+ "    @Id\n" //$NON-NLS-1$
				+ "    @GeneratedValue(strategy = GenerationType.IDENTITY)\n" //$NON-NLS-1$
				+ "    private Long id;\n\n" //$NON-NLS-1$
				+ "    private String name;\n\n" //$NON-NLS-1$
				+ "    public Long getId() { return id; }\n" //$NON-NLS-1$
				+ "    public void setId(Long id) { this.id = id; }\n" //$NON-NLS-1$
				+ "    public String getName() { return name; }\n" //$NON-NLS-1$
				+ "    public void setName(String name) { this.name = name; }\n" //$NON-NLS-1$
				+ "}\n"; //$NON-NLS-1$
	}

	private static String memberRepositoryJava(String basePackage) {
		return "package " + basePackage + ".repository;\n\n" //$NON-NLS-1$ //$NON-NLS-2$
				+ "import org.springframework.data.jpa.repository.JpaRepository;\n\n" //$NON-NLS-1$
				+ "import " + basePackage + ".entity.MemberEntity;\n\n" //$NON-NLS-1$ //$NON-NLS-2$
				+ "public interface MemberRepository extends JpaRepository<MemberEntity, Long> {\n" //$NON-NLS-1$
				+ "}\n"; //$NON-NLS-1$
	}

	private static String memberServiceJava(String basePackage) {
		return "package " + basePackage + ".service;\n\n" //$NON-NLS-1$ //$NON-NLS-2$
				+ "import java.util.List;\n\n" //$NON-NLS-1$
				+ "import org.springframework.stereotype.Service;\n\n" //$NON-NLS-1$
				+ "import " + basePackage + ".entity.MemberEntity;\n" //$NON-NLS-1$ //$NON-NLS-2$
				+ "import " + basePackage + ".repository.MemberRepository;\n\n" //$NON-NLS-1$ //$NON-NLS-2$
				+ "@Service\n" //$NON-NLS-1$
				+ "public class MemberService {\n" //$NON-NLS-1$
				+ "    private final MemberRepository memberRepository;\n\n" //$NON-NLS-1$
				+ "    public MemberService(MemberRepository memberRepository) {\n" //$NON-NLS-1$
				+ "        this.memberRepository = memberRepository;\n" //$NON-NLS-1$
				+ "    }\n\n" //$NON-NLS-1$
				+ "    public List<MemberEntity> findAll() {\n" //$NON-NLS-1$
				+ "        return memberRepository.findAll();\n" //$NON-NLS-1$
				+ "    }\n" //$NON-NLS-1$
				+ "}\n"; //$NON-NLS-1$
	}

	private static String mainClassName(String projectName) {
		if (projectName == null || projectName.isBlank()) {
			return "SpringDemoApplication"; //$NON-NLS-1$
		}
		String[] tokens = projectName.replaceAll("[^A-Za-z0-9]+", " ").trim().split("\\s+"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		StringBuilder builder = new StringBuilder();
		for (String token : tokens) {
			if (token.isBlank()) {
				continue;
			}
			builder.append(Character.toUpperCase(token.charAt(0)));
			if (token.length() > 1) {
				builder.append(token.substring(1).toLowerCase());
			}
		}
		if (builder.length() == 0) {
			return "SpringDemoApplication"; //$NON-NLS-1$
		}
		return builder.append("Application").toString(); //$NON-NLS-1$
	}

	private static String mainClassFileName(boolean springBoot, String projectName) {
		return springBoot ? mainClassName(projectName) + ".java" : "App.java"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static String mainTestFileName(boolean springBoot, String projectName) {
		return springBoot ? mainClassName(projectName) + "Tests.java" : "AppTest.java"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	private enum BuildTool {
		GRADLE, MAVEN
	}
}
