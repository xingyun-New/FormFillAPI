# Email API Test Script
# Test the modified email sending API functionality

Write-Host "=== Email API Testing ===" -ForegroundColor Cyan
Write-Host "Waiting for service to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

function Test-EmailAPI {
    param(
        [string]$TestName,
        [hashtable]$RequestBody
    )
    
    Write-Host "`n--- $TestName ---" -ForegroundColor Magenta
    
    $body = $RequestBody | ConvertTo-Json -Depth 10
    $utf8 = [System.Text.Encoding]::UTF8.GetBytes($body)
    
    Write-Host "Request Body: $body" -ForegroundColor Gray
    
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8080/api/email/send" -Method POST -Body $utf8 -ContentType "application/json; charset=utf-8" -TimeoutSec 30
        
        Write-Host "✓ $TestName - Success" -ForegroundColor Green
        Write-Host "  Subject: $($response.emailSubject)"
        Write-Host "  Recipients: $($response.recipients -join ', ')"
        if ($response.attachmentName) {
            Write-Host "  Attachment: $($response.attachmentName)"
        } else {
            Write-Host "  Attachment: None" -ForegroundColor Yellow
        }
        return $true
    } catch {
        Write-Host "✗ $TestName - Failed: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.ErrorDetails.Message) {
            try {
                $errorDetails = $_.ErrorDetails.Message | ConvertFrom-Json
                Write-Host "  Error: $($errorDetails.message)"
            } catch {
                Write-Host "  Error Details: $($_.ErrorDetails.Message)"
            }
        }
        return $false
    }
}

# Test 1: With attachment and custom content
$test1 = @{
    download_url = "http://localhost:8080/api/download/test.xlsx"
    formName = "Test Form 1"
    formStatus = "RPAProcess"
    mailContent = "This is custom email content for testing"
    mailTitle = "Custom Email Title - Test 1"
}

# Test 2: No attachment
$test2 = @{
    formName = "Test Form 2"
    formStatus = "Completed"
    mailContent = "Email without attachment - test completed"
    mailTitle = "No Attachment Test"
}

# Test 3: Default template
$test3 = @{
    formName = "Test Form 3"
    formStatus = "RPAProcess"
}

# Test 4: Minimal required fields only
$test4 = @{
    formName = "Test Form 4"
    formStatus = "Completed"
}

# Run tests
$results = @()

$results += Test-EmailAPI -TestName "Test 1: With attachment and custom content" -RequestBody $test1
$results += Test-EmailAPI -TestName "Test 2: No attachment email" -RequestBody $test2
$results += Test-EmailAPI -TestName "Test 3: Default template" -RequestBody $test3
$results += Test-EmailAPI -TestName "Test 4: Minimal email" -RequestBody $test4

# Summary
$successCount = ($results | Where-Object { $_ -eq $true }).Count
$totalCount = $results.Count

Write-Host "`n=== Test Results Summary ===" -ForegroundColor Cyan
Write-Host "Total Tests: $totalCount"
Write-Host "Successful: $successCount" -ForegroundColor Green
Write-Host "Failed: $($totalCount - $successCount)" -ForegroundColor Red

if ($successCount -eq $totalCount) {
    Write-Host "🎉 All tests passed! Email API modification successful!" -ForegroundColor Green
} else {
    Write-Host "⚠️ Some tests failed, please check the logs" -ForegroundColor Yellow
} 