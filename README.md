# MySystemUI
重构初期
------------------
插件版本     	Gradle版本
1.0.0 - 1.1.3	2.2.1 - 2.3
1.2.0 - 1.3.1	2.2.1 - 2.9
1.5.0	        2.2.1 - 2.13
2.0.0 - 2.1.2	2.10 - 2.13
2.1.3 - 2.2.3	2.14.1+
2.3.0+	        3.3+
3.0.0+	        4.1+
3.1.0+	        4.4+
3.2.0 - 3.2.1	4.6+
3.3.0 - 3.3.2	4.10.1+
3.4.0+	        5.1.1+

下载gradle并解压gradle-4.4-all.zip：gradle:3.1.2
https\://services.gradle.org/distributions/gradle-4.4-all.zip
配置环境变量
GRADLE_HOME = XXX/gradle-4.4-all/bin
cmd窗口执行gradle -v
配置项目gradle为本地XXX/gradle-4.4-all目录
---------------------
设置gradle镜像
全局配置（操作系统的当前用户）
在操作系统当前用户的 HOME 目录，找到 .gradle 文件夹，在这个文件夹下面创建一个文本文件 init.gradle，完整的文件路径可能如下：C:\Users<june>.gradle\init.gradle。
在此文件中加入如下代码片段：
allprojects{
    repositories {
        def ALIYUN_REPOSITORY_URL = 'http://maven.aliyun.com/nexus/content/groups/public'
        def ALIYUN_JCENTER_URL = 'http://maven.aliyun.com/nexus/content/repositories/jcenter'
        all { ArtifactRepository repo ->
            if(repo instanceof MavenArtifactRepository){
                def url = repo.url.toString()
                if (url.startsWith('https://repo1.maven.org/maven2')) {
                    project.logger.lifecycle "Repository ${repo.url} replaced by $ALIYUN_REPOSITORY_URL."
                    remove repo
                }
                if (url.startsWith('https://jcenter.bintray.com/')) {
                    project.logger.lifecycle "Repository ${repo.url} replaced by $ALIYUN_JCENTER_URL."
                    remove repo
                }
            }
        }
        maven {
            url ALIYUN_REPOSITORY_URL
            url ALIYUN_JCENTER_URL
        }
    }
}
------------------
项目级配置（推荐）
修改buildscript->repositories
添加
allprojects {
    repositories {
        maven { url 'http://maven.aliyun.com/nexus/content/groups/public/' }
        maven{ url 'http://maven.aliyun.com/nexus/content/repositories/jcenter'}
    }
}

添加后如下：
buildscript {
    repositories {
        maven { url 'http://maven.aliyun.com/nexus/content/groups/public/' }
        maven{ url 'http://maven.aliyun.com/nexus/content/repositories/jcenter'}
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:2.2.3'

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        maven { url 'http://maven.aliyun.com/nexus/content/groups/public/' }
        maven{ url 'http://maven.aliyun.com/nexus/content/repositories/jcenter'}
    }
}
------------------------------------------------