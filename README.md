# annonations

## Add it in your root build.gradle at the end of repositories:
```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

## Step 2. Add the dependency
```groovy
dependencies {
    annotationProcessor 'com.github.yangzc:processor:1.0.3'
}
```

## Step 3. Modify package
```
android {
    defaultConfig {
        javaCompileOptions {
            //配置注解处理器参数
            annotationProcessorOptions {
                arguments = [ applicationId : "com.hyena.support" ]
            }
        }
    }
}
```

