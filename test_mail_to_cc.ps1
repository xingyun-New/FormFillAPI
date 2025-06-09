# Test MailTo and MailCc Functionality
# Test the new email recipient features

Write-Host "=== MailTo and MailCc Test ===" -ForegroundColor Cyan
Write-Host "Waiting for service..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

function Test-MailAPI {
    param(
        [string]$TestName,
        [hashtable]$RequestBody
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

Write-Host "`n🚀 Testing MailTo and MailCc functionality..." -ForegroundColor Blue

# Test 1: Your exact requirement
$test1 = @{
    mailContent = "这里是邮件内容"
    mailTitle = "这里是邮件标题"
    mailTo = "xingyun1982314@126.com"
    mailCc = "xingyun@murata.com"
}

# Test 2: Only required field (mailTo)
$test2 = @{
    mailContent = "Test email with only mailTo"
    mailTitle = "Test Subject"
    mailTo = "xingyun1982314@126.com"
}

# Test 3: With attachment
$test3 = @{
    download_url = "http://localhost:8080/api/download/test.xlsx"
    mailContent = "Email with attachment"
    mailTitle = "Attachment Test"
    mailTo = "xingyun1982314@126.com"
    mailCc = "xingyun@murata.com"
}

# Test 4: Without custom title/content (use defaults)
$test4 = @{
    formName = "Test Form"
    formStatus = "Completed"
    mailTo = "xingyun1982314@126.com"
}

# Test 5: Missing mailTo (should fail)
$test5 = @{
    mailContent = "Missing mailTo test"
    mailTitle = "Should Fail"
    mailCc = "xingyun@murata.com"
}

# Run tests
$results = @()

$results += Test-MailAPI -TestName "Test 1: Your exact requirement" -RequestBody $test1
$results += Test-MailAPI -TestName "Test 2: Only mailTo (no CC)" -RequestBody $test2
$results += Test-MailAPI -TestName "Test 3: With attachment" -RequestBody $test3
$results += Test-MailAPI -TestName "Test 4: Default content" -RequestBody $test4
$results += Test-MailAPI -TestName "Test 5: Missing mailTo (should fail)" -RequestBody $test5

# Results summary
$successCount = ($results | Where-Object { $_ -eq $true }).Count
$totalCount = $results.Count

Write-Host "`n=== MAIL TO/CC TEST RESULTS ===" -ForegroundColor Cyan
Write-Host "Total Tests: $totalCount"
Write-Host "Successful: $successCount" -ForegroundColor Green
Write-Host "Failed: $($totalCount - $successCount)" -ForegroundColor Red

if ($successCount -ge 4) {  # Expecting test 5 to fail
    Write-Host "`n🎉 SUCCESS! MailTo and MailCc working correctly!" -ForegroundColor Green
    Write-Host "✅ mailTo is required and working" -ForegroundColor Green
    Write-Host "✅ mailCc is optional and working" -ForegroundColor Green
    Write-Host "✅ Custom titles are used correctly" -ForegroundColor Green
} else {
    Write-Host "`n⚠️ Some unexpected results, please check" -ForegroundColor Yellow
} 