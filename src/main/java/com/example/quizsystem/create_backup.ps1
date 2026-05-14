
$ErrorActionPreference = "Stop"

$sourceDir = "C:\Users\User\IdeaProjects\QuizSystem"
$destDir = "C:\Users\User\IdeaProjects\ExamandQuizSystemsOriginal"

Write-Host "Creating $destDir from the initial commit..."

if (Test-Path $destDir) {
    Remove-Item -Recurse -Force $destDir
}

# Use git worktree to extract the initial commit
Set-Location $sourceDir
git worktree add $destDir a58dd01

Set-Location $destDir

# Rename package directory
$oldPkg = "src\main\java\com\example\quizsystem"
$newPkg = "src\main\java\com\example\examandquizsystems"

if (Test-Path $oldPkg) {
    Rename-Item $oldPkg "examandquizsystems"
}

# Ensure pom.xml exists, if not copy from current
if (-not (Test-Path "pom.xml")) {
    Copy-Item "$sourceDir\pom.xml" "pom.xml"
}

# Replace quizsystem with examandquizsystems in all text files
Get-ChildItem -Path . -Recurse -File -Exclude "*.jar", "*.class", "*.png", "*.jpg", ".git" | ForEach-Object {
    $content = Get-Content $_.FullName -Raw
    if ($content -match "quizsystem" -or $content -match "QuizSystem") {
        $newContent = $content -cReplace "quizsystem", "examandquizsystems"
        $newContent = $newContent -cReplace "QuizSystem", "ExamandQuizSystems"
        Set-Content $_.FullName $newContent
    }
}

# Clean up git worktree link
Remove-Item .git -Force
Set-Location $sourceDir
git worktree prune

Write-Host ""
Write-Host "======================================================"
Write-Host "SUCCESS!"
Write-Host "Your original project is now at: $destDir"
Write-Host "You can open this folder in IntelliJ!"
Write-Host "======================================================"
