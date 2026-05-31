# Create bin directory if it doesn't exist
if (-not (Test-Path bin)) {
    New-Item -ItemType Directory bin -Force
}

# Find javac executable
$javacPath = "javac"
if (Test-Path "C:\Program Files\Java\jdk1.8.0_202\bin\javac.exe") {
    $javacPath = "C:\Program Files\Java\jdk1.8.0_202\bin\javac.exe"
} elseif ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\javac.exe")) {
    $javacPath = "$env:JAVA_HOME\bin\javac.exe"
}

# Collect all Java files and compile them
$javaFiles = Get-ChildItem -Path src -Filter *.java -Recurse | ForEach-Object { '"' + $_.FullName.Replace('\', '/') + '"' }
if ($javaFiles) {
    [System.IO.File]::WriteAllLines("sources.txt", $javaFiles)
    & $javacPath -cp "lib/json.jar" -d bin -encoding utf8 `@sources.txt
    Remove-Item sources.txt
    Write-Host "Compilation complete." -ForegroundColor Green
} else {
    Write-Host "No Java files found to compile." -ForegroundColor Yellow
}
