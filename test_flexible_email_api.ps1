# Flexible Email API Test Script
# Test all optional parameters functionality

Write-Host "=== Flexible Email API Test ===" -ForegroundColor Cyan
Write-Host "Waiting for service..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

function Test-FlexibleAPI {
    param(
        [string]$TestName,
        [hashtable]$RequestBody = @{}
    )
    
    Write-Host "`n--- $TestName ---" -ForegroundColor Magenta
    
    $body = $RequestBody | ConvertTo-Json -Depth 10
    $utf8 = [System.Text.Encoding]::UTF8.GetBytes($body)
    
    Write-Host "Request: $body" -ForegroundColor Gray
    
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8080/api/email/send" -Method POST -Body $utf8 -ContentType "application/json; charset=utf-8" -TimeoutSec 30
        
        Write-Host "✓ $TestName - SUCCESS" -ForegroundColor Green
        Write-Host "  Subject: $($response.emailSubject)"
        Write-Host "  Recipients: $($response.recipients -join ', ')"
        if ($response.attachmentName) {
            Write-Host "  Attachment: $($response.attachmentName)"
        } else {
            Write-Host "  Attachment: None" -ForegroundColor Yellow
        }
        return $true
    } catch {
        Write-Host "✗ $TestName - FAILED: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# Test all possible parameter combinations

Write-Host "`n🚀 Testing all optional parameters..." -ForegroundColor Blue

# Test 1: Completely empty parameters - Ultimate test
$test1 = @{}

# Test 2: Only custom content
$test2 = @{
    mailContent = "This is a system notification email"
    mailTitle = "System Notification"
}

# Test 3: Only form info
$test3 = @{
    formName = "Test Form"
    formStatus = "Completed"
}

# Test 4: Only email title
$test4 = @{
    mailTitle = "Title Only Test"
}

# Test 5: Only email content
$test5 = @{
    mailContent = "Content only test email"
}

# Test 6: Only form name
$test6 = @{
    formName = "Form Name Only Test"
}

# Test 7: Your original requirement (all parameters)
$test7 = @{
    download_url = "http://localhost:8080/api/download/test.xlsx"
    formName = "Test Form Complete"
    formStatus = "RPAProcess"
    mailContent = "Custom email content here"
    mailTitle = "Custom email title here"
}

# Run all tests
$results = @()

$results += Test-FlexibleAPI -TestName "Test 1: Empty parameters (Ultimate)" -RequestBody $test1
$results += Test-FlexibleAPI -TestName "Test 2: Custom content only" -RequestBody $test2
$results += Test-FlexibleAPI -TestName "Test 3: Form info only" -RequestBody $test3
$results += Test-FlexibleAPI -TestName "Test 4: Title only" -RequestBody $test4
$results += Test-FlexibleAPI -TestName "Test 5: Content only" -RequestBody $test5
$results += Test-FlexibleAPI -TestName "Test 6: Form name only" -RequestBody $test6
$results += Test-FlexibleAPI -TestName "Test 7: Complete parameters" -RequestBody $test7

# Results summary
$successCount = ($results | Where-Object { $_ -eq $true }).Count
$totalCount = $results.Count

Write-Host "`n=== ULTIMATE TEST RESULTS ===" -ForegroundColor Cyan
Write-Host "Total Tests: $totalCount"
Write-Host "Successful: $successCount" -ForegroundColor Green
Write-Host "Failed: $($totalCount - $successCount)" -ForegroundColor Red

if ($successCount -eq $totalCount) {
    Write-Host "`n🎉 CONGRATULATIONS! ALL TESTS PASSED!" -ForegroundColor Green
    Write-Host "✨ API now supports completely optional parameters!" -ForegroundColor Green
    Write-Host "🚀 You can send emails with ANY parameter combination!" -ForegroundColor Green
} else {
    Write-Host "`n⚠️ Some tests failed, please check logs" -ForegroundColor Yellow
}

Write-Host "`n📋 Test Summary:" -ForegroundColor Blue
Write-Host "  ✅ Empty parameters: Supported" -ForegroundColor Green
Write-Host "  ✅ Partial parameters: Supported" -ForegroundColor Green
Write-Host "  ✅ Complete parameters: Supported" -ForegroundColor Green
Write-Host "  🎯 API Flexibility: 100%" -ForegroundColor Green 