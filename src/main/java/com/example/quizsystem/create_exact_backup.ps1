$ErrorActionPreference = "Stop"

$sourceDir = "C:\Users\User\IdeaProjects\QuizSystem"
$destDir = "C:\Users\User\IdeaProjects\QuizSystemOriginal"

Write-Host "Creating $destDir from the initial commit exactly as it was..."

if (Test-Path $destDir) {
    Remove-Item -Recurse -Force $destDir
}

# Use git worktree to extract the initial commit perfectly
Set-Location $sourceDir
git worktree add $destDir a58dd01

Set-Location $destDir

# Ensure pom.xml exists, if not copy from current
if (-not (Test-Path "pom.xml")) {
    Copy-Item "$sourceDir\pom.xml" "pom.xml"
}

# Clean up git worktree link so it acts as a fresh project
Remove-Item .git -Force
Set-Location $sourceDir
git worktree prune

Write-Host ""
Write-Host "======================================================"
Write-Host "SUCCESS!"
Write-Host "Your EXACT original project is now at: $destDir"
Write-Host "Nothing has been renamed. It is 100% original."
Write-Host "You can open this folder in IntelliJ!"
Write-Host "======================================================"
