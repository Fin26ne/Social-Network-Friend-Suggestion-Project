# Create bin directory if it doesn't exist
if (-not (Test-Path bin)) {
    New-Item -ItemType Directory bin -Force
}

# Collect all Java files and compile them
$javaFiles = Get-ChildItem -Path src -Filter *.java -Recurse | ForEach-Object { '"' + $_.FullName.Replace('\', '/') + '"' }
if ($javaFiles) {
    [System.IO.File]::WriteAllLines("sources.txt", $javaFiles)
    & "C:\Program Files\Java\jdk1.8.0_202\bin\javac.exe" -cp "lib/json.jar" -d bin -encoding utf8 `@sources.txt
    Remove-Item sources.txt
    Write-Host "Compilation complete." -ForegroundColor Green
} else {
    Write-Host "No Java files found to compile." -ForegroundColor Yellow
}
