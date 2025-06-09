# Quick test for your exact requirement
$body = @{
    mailContent = "This is email content"
    mailTitle = "This is email title"
    mailTo = "xingyun1982314@126.com"
    mailCc = "xingyun@murata.com"
} | ConvertTo-Json

$utf8 = [System.Text.Encoding]::UTF8.GetBytes($body)

Write-Host "Testing your exact requirement..." -ForegroundColor Cyan
Write-Host "Request body:" -ForegroundColor Gray
Write-Host $body

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/email/send" -Method POST -Body $utf8 -ContentType "application/json; charset=utf-8"
    
    Write-Host "`n✅ SUCCESS! Your requirement is working!" -ForegroundColor Green
    Write-Host "Subject: $($response.emailSubject)" -ForegroundColor White
    Write-Host "Recipients: $($response.recipients -join ', ')" -ForegroundColor White
    Write-Host "`n📧 Email sent to: $($response.recipients[0])" -ForegroundColor Green
    Write-Host "📧 CC sent to: $($response.recipients[1])" -ForegroundColor Green
    
} catch {
    Write-Host "`n❌ FAILED: $($_.Exception.Message)" -ForegroundColor Red
} 