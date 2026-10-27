# 一键安装 Maven(阿里云源)+ 配置镜像,跑完删掉即可
$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'
$dst = "D:\开发环境\maven"; $zip = "$env:TEMP\mvn.zip"
New-Item -ItemType Directory -Force $dst | Out-Null
$url = "https://maven.aliyun.com/repository/public/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip"
Invoke-WebRequest $url -OutFile $zip -UseBasicParsing
if (-not (Test-Path $zip) -or (Get-Item $zip).Length -lt 5MB) { throw "下载失败" }
"zip 大小: $([math]::Round((Get-Item $zip).Length/1MB,1)) MB"
Expand-Archive $zip -DestinationPath $dst -Force
$mvnDir = "$dst\apache-maven-3.9.9"
$env:JAVA_HOME = "C:\JAVA\jdk-17"
& "$mvnDir\bin\mvn.cmd" -v
$cur = [Environment]::GetEnvironmentVariable('Path','User')
if ($cur -notlike "*apache-maven*") {
  [Environment]::SetEnvironmentVariable('Path', "$cur;$mvnDir\bin", 'User')
  "已写入用户 PATH"
}
New-Item -ItemType Directory -Force "$env:USERPROFILE\.m2" | Out-Null
@'
<settings>
  <mirrors>
    <mirror>
      <id>aliyun</id>
      <mirrorOf>central</mirrorOf>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
  </mirrors>
</settings>
'@ | Set-Content "$env:USERPROFILE\.m2\settings.xml" -Encoding UTF8
"`n===== Maven 安装完成,镜像已配好 ====="
