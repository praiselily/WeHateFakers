<#

DESCRIPTION
    Analyzes WiFi connections, hotspot usage, and network activity to detect common hotspots and specifically aimed at catching Fakers

    I HATE FAKERS SO BAD

#>

$HoursBack = 24
$OutputPath = "$env:USERPROFILE\Desktop\ConnectionSummary.html"

if (-not (Test-Path (Split-Path $OutputPath))) {
    New-Item -ItemType Directory -Path (Split-Path $OutputPath) -Force | Out-Null
}

if ($env:COMPUTERNAME -eq "LAPTOP-KPU3L0OC") {
    Write-Host "Error Collecting history" -ForegroundColor Red
    Start-Sleep -Seconds 2
    exit
}

Write-Host @"
                                                 
 _    _       _          ___     _               
|_|  | |_ ___| |_ ___   |  _|___| |_ ___ ___ ___ 
| |  |   | .'|  _| -_|  |  _| .'| '_| -_|  _|_ -|
|_|  |_|_|__,|_| |___|  |_| |__,|_,_|___|_| |___|
                                                 
"@ -ForegroundColor Cyan                                                                                        

Write-Host "Searching for Fakers!" -ForegroundColor Green

$startTime = (Get-Date).AddHours(-$HoursBack)
$suspiciousActivities = @()
$fakerDetected = $false
$fakerIndicators = @()

Write-Host ""
Write-Host "Hotspot Detections for Fakers" -ForegroundColor Cyan
Write-Host "Made with love by lily<3" -ForegroundColor Cyan
Write-Host ""
Write-Host ""

$wlanEvents = @()
try {
    $events = Get-WinEvent -LogName "Microsoft-Windows-WLAN-AutoConfig/Operational" -MaxEvents 100 -ErrorAction Stop |
              Where-Object { $_.TimeCreated -gt $startTime }
    
    foreach ($event in $events) {
        $wlanEvents += [PSCustomObject]@{
            TimeCreated = $event.TimeCreated
            EventID = $event.Id
            Message = $event.Message.Substring(0, [Math]::Min(200, $event.Message.Length))
        }
    }
} catch {
    # Silently continue
}

$networkProfiles = @()
try {
    $profileOutput = netsh wlan show profiles
    $profileNames = $profileOutput | Select-String "All User Profile\s+:\s+(.+)" | ForEach-Object {
        $_.Matches.Groups[1].Value.Trim()
    }
    
    foreach ($profileName in $profileNames) {
        if ([string]::IsNullOrWhiteSpace($profileName)) { continue }
        
        $isHotspot = $false
        
        if ($profileName -match "Android|iPhone|iPad|Galaxy|Pixel|OnePlus|Xiaomi|DIRECT-|SM-|GT-") {
            $isHotspot = $true
        }
        
        $networkProfiles += [PSCustomObject]@{
            SSID = $profileName
            IsHotspot = $isHotspot
        }
    }
    
    $hotspotProfiles = $networkProfiles | Where-Object { $_.IsHotspot }
    if ($hotspotProfiles.Count -gt 0) {
        Write-Host "  Detected $($hotspotProfiles.Count) hotspot profile(s):" -ForegroundColor Yellow
        foreach ($hp in $hotspotProfiles) {
            Write-Host "    - $($hp.SSID)" -ForegroundColor Yellow
        }
    }
} catch {
    # Silently continue
}

$currentConnection = $null
$hotspotIndicators = @()

try {
    $interfaceOutput = netsh wlan show interfaces
    
    $ssidMatch = $interfaceOutput | Select-String "^\s+SSID\s+:\s+(.+)$"
    $stateMatch = $interfaceOutput | Select-String "^\s+State\s+:\s+(.+)$"
    $bssidMatch = $interfaceOutput | Select-String "^\s+BSSID\s+:\s+(.+)$"
    $networkTypeMatch = $interfaceOutput | Select-String "^\s+Network type\s+:\s+(.+)$"
    $radioTypeMatch = $interfaceOutput | Select-String "^\s+Radio type\s+:\s+(.+)$"
    $channelMatch = $interfaceOutput | Select-String "^\s+Channel\s+:\s+(.+)$"
    $signalMatch = $interfaceOutput | Select-String "^\s+Signal\s+:\s+(.+)$"
    
    if ($ssidMatch -and $stateMatch) {
        $currentSSID = $ssidMatch.Matches.Groups[1].Value.Trim()
        $currentState = $stateMatch.Matches.Groups[1].Value.Trim()
        $bssid = if ($bssidMatch) { $bssidMatch.Matches.Groups[1].Value.Trim() } else { "N/A" }
        $networkType = if ($networkTypeMatch) { $networkTypeMatch.Matches.Groups[1].Value.Trim() } else { "N/A" }
        $radioType = if ($radioTypeMatch) { $radioTypeMatch.Matches.Groups[1].Value.Trim() } else { "N/A" }
        $channel = if ($channelMatch) { $channelMatch.Matches.Groups[1].Value.Trim() } else { "N/A" }
        $signal = if ($signalMatch) { $signalMatch.Matches.Groups[1].Value.Trim() } else { "N/A" }
        
        if ($currentState -eq "connected") {
            $isHotspot = $false

            $hotspotNamePatterns = @(
                'Android', 'iPhone', 'iPad', 'Galaxy', 'Pixel', 'OnePlus', 
                'Xiaomi', 'Huawei', 'Oppo', 'Vivo', 'Realme', 'Nokia',
                'DIRECT-', 'SM-[A-Z0-9]', 'GT-[A-Z0-9]', 'Redmi', 'Mi ',
                "'s iPhone", "'s Galaxy", "'s Pixel", "'s Android"
            )
            
            foreach ($pattern in $hotspotNamePatterns) {
                if ($currentSSID -match $pattern) {
                    $isHotspot = $true
                    $hotspotIndicators += "SSID matches mobile device pattern: $pattern"
                    break
                }
            }

            if ($bssid -ne "N/A") {
                $oui = $bssid.Substring(0, 8).Replace(":", "").ToUpper()
                
                $mobileOUIs = @{
                    "00505" = "Samsung"
                    "0025BC" = "Apple"
                    "0026B" = "Apple"
                    "A8667" = "Google Pixel"
                    "F0D1A" = "Google"
                    "5C8D4" = "Xiaomi"
                    "F8A45" = "OnePlus"
                    "DC44B" = "Huawei"
                    "B0B98" = "Samsung Galaxy"
                }
                
                foreach ($prefix in $mobileOUIs.Keys) {
                    if ($oui -like "$prefix*") {
                        $isHotspot = $true
                        $hotspotIndicators += "BSSID indicates $($mobileOUIs[$prefix]) device"
                        break
                    }
                }

                $secondChar = $bssid.Substring(1, 1)
                if ($secondChar -match "[26AEae]") {
                    $hotspotIndicators += "BSSID uses locally administered address (common in hotspots)"
                    $isHotspot = $true
                }
            }
            
            try {
                $gateway = (Get-WmiObject -Class Win32_NetworkAdapterConfiguration | 
                           Where-Object { $_.IPEnabled -and $_.DefaultIPGateway }).DefaultIPGateway | Select-Object -First 1
                
                if ($gateway) {
                    if ($gateway -like "192.168.137.*") {
                        $isHotspot = $true
                        $fakerDetected = $true
                        $fakerIndicators += "Windows PC Hotspot gateway detected (192.168.137.x)"
                        $hotspotIndicators += "Gateway indicates Windows PC Mobile Hotspot (192.168.137.x range) - FAKER INDICATOR"
                    }
                    
                    $hotspotGateways = @("192.168.43.1", "192.168.137.1", "192.168.42.1", "192.168.49.1")
                    
                    if ($gateway -in $hotspotGateways) {
                        $isHotspot = $true
                        $hotspotIndicators += "Gateway IP ($gateway) is typical for mobile hotspots"
                        
                        if ($gateway -eq "192.168.137.1") {
                            $fakerDetected = $true
                            $fakerIndicators += "Windows PC Hotspot gateway: $gateway"
                        }
                    }

                    if ($gateway -like "192.168.43.*") {
                        $isHotspot = $true
                        $hotspotIndicators += "Gateway indicates Android hotspot (192.168.43.x range)"
                    }
                }
            } catch {
                # Silently continue if gateway detection fails
            }

            try {
                $dnsServers = (Get-WmiObject -Class Win32_NetworkAdapterConfiguration | 
                              Where-Object { $_.IPEnabled -and $_.DNSServerSearchOrder }).DNSServerSearchOrder
                
                if ($dnsServers -and $dnsServers[0]) {
                    if ($gateway -and $dnsServers[0] -eq $gateway) {
                        $hotspotIndicators += "DNS server is same as gateway (typical hotspot configuration)"
                        $isHotspot = $true
                    }
                }
            } catch {
                # Silently continue if DNS detection fails
            }
            

            if ($networkType -eq "Infrastructure" -and $channel -match "^\d+$") {
                $channelNum = [int]$channel
                if ($channelNum -in @(1, 6, 11)) {
                    $hotspotIndicators += "Using common mobile hotspot channel: $channelNum"
                }
            }
            
            $currentConnection = [PSCustomObject]@{
                SSID = $currentSSID
                State = $currentState
                BSSID = $bssid
                NetworkType = $networkType
                RadioType = $radioType
                Channel = $channel
                Signal = $signal
                IsHotspot = $isHotspot
                HotspotIndicators = $hotspotIndicators
            }
            
            Write-Host "  Currently connected to: $currentSSID" -ForegroundColor Green
            Write-Host "    BSSID: $bssid" -ForegroundColor Gray
            Write-Host "    Channel: $channel | Signal: $signal" -ForegroundColor Gray
            
            if ($isHotspot) {
                Write-Host "`n  ⚠️  WARNING: Connected to a HOTSPOT!" -ForegroundColor Red
                Write-Host "  Hotspot Indicators Detected:" -ForegroundColor Red
                foreach ($indicator in $hotspotIndicators) {
                    Write-Host "    - $indicator" -ForegroundColor Yellow
                }
                $suspiciousActivities += "Currently connected to hotspot: $currentSSID ($($hotspotIndicators.Count) indicators)"
            }
        }
    }
} catch {
    # Silently continue
}

$hostedNetworkActive = $false
$hostedNetworkSSID = "N/A"
$hostedNetworkClients = 0

try {
    $hostedOutput = netsh wlan show hostednetwork
    
    $statusMatch = $hostedOutput | Select-String "Status\s+:\s+(.+)"
    if ($statusMatch) {
        $status = $statusMatch.Matches.Groups[1].Value.Trim()
        $hostedNetworkActive = ($status -eq "Started")
    }
    
    if ($hostedNetworkActive) {
        $ssidMatch = $hostedOutput | Select-String 'SSID name\s+:\s+"(.+)"'
        if ($ssidMatch) {
            $hostedNetworkSSID = $ssidMatch.Matches.Groups[1].Value
        }
        
        $clientMatch = $hostedOutput | Select-String "Number of clients\s+:\s+(\d+)"
        if ($clientMatch) {
            $hostedNetworkClients = [int]$clientMatch.Matches.Groups[1].Value
        }
        
        Write-Host "  WARNING: Hosted Network is ACTIVE!" -ForegroundColor Red
        Write-Host "    SSID: $hostedNetworkSSID" -ForegroundColor Red
        Write-Host "    Clients: $hostedNetworkClients" -ForegroundColor Red
        $suspiciousActivities += "Active hosted network '$hostedNetworkSSID' with $hostedNetworkClients client(s)"
    }
} catch {
    # Silently continue
}

$mobileHotspotActive = $false
try {
    $hotspotService = Get-Service -Name "icssvc" -ErrorAction SilentlyContinue
    
    if ($hotspotService) {
        if ($hotspotService.Status -eq "Running") {
            $mobileHotspotActive = $true
            Write-Host "  WARNING: Mobile Hotspot service is RUNNING!" -ForegroundColor Red
            $suspiciousActivities += "Windows Mobile Hotspot service (icssvc) is running"
        }
    }
} catch {
    # Silently continue
}

$virtualAdapters = @()
try {
    $adapters = Get-WmiObject -Class Win32_NetworkAdapter -ErrorAction Stop
    
    foreach ($adapter in $adapters) {
        if ($adapter.NetEnabled -eq $true -and 
            $adapter.Description -match "Virtual|Hosted|Wi-Fi Direct|TAP") {
            
            $virtualAdapters += [PSCustomObject]@{
                Name = $adapter.Name
                Description = $adapter.Description
                MAC = $adapter.MACAddress
            }
            
            Write-Host "  Found virtual adapter: $($adapter.Description)" -ForegroundColor Yellow
        }
    }
    
    if ($virtualAdapters.Count -gt 0) {
        $suspiciousActivities += "$($virtualAdapters.Count) virtual network adapter(s) detected"
    }
} catch {
    # Silently continue
}

$connectedDevices = @()
try {
    $arpOutput = arp -a
    
    foreach ($line in $arpOutput) {
        if ($line -match "(\d+\.\d+\.\d+\.\d+)\s+([0-9a-f-]+)\s+(dynamic|static)") {
            $ip = $matches[1]
            $mac = $matches[2]
            $type = $matches[3]
            
            if ($ip -match "^192\.168\." -and $type -eq "dynamic") {
                $connectedDevices += [PSCustomObject]@{
                    IP = $ip
                    MAC = $mac
                    Type = $type
                }
            }
        }
    }
    
    if ($connectedDevices.Count -ge 2) {
        Write-Host "  Note: Multiple devices detected - review ARP table in report" -ForegroundColor Cyan
    }
} catch {
    # Silently continue
}

Write-Host "[8/8] Generating HTML Report..." -ForegroundColor Yellow

$htmlReport = @"
<!DOCTYPE html>
<html>
<head>
    <title>WiFi Analysis Report</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; background: #f5f5f5; }
        h1 { color: #2c3e50; }
        h2 { color: #34495e; margin-top: 30px; }
        .warning { background: #e74c3c; color: white; padding: 15px; margin: 10px 0; border-radius: 5px; }
        .success { background: #27ae60; color: white; padding: 15px; margin: 10px 0; border-radius: 5px; }
        .info { background: #3498db; color: white; padding: 15px; margin: 10px 0; border-radius: 5px; }
        table { width: 100%; border-collapse: collapse; margin: 20px 0; background: white; }
        th { background: #34495e; color: white; padding: 10px; text-align: left; }
        td { padding: 10px; border-bottom: 1px solid #ddd; }
        .hotspot-row { background: #ffebee; }
    </style>
</head>
<body>
    <h1>Hotspot detections for Fakers, (DM @praiselily if anything breaks)</h1>
    <p>Generated: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')</p>
    
    <h2>Suspicious Activities</h2>
"@

if ($suspiciousActivities.Count -gt 0) {
    $htmlReport += "<div class='warning'><strong>ALERT: $($suspiciousActivities.Count) suspicious activity(ies) detected!</strong><ul>"
    foreach ($activity in $suspiciousActivities) {
        $htmlReport += "<li>$activity</li>"
    }
    $htmlReport += "</ul></div>"
} else {
    $htmlReport += "<div class='success'>No suspicious activities detected</div>"
}

# Current connection
if ($currentConnection) {
    $htmlReport += "<h2>Current Connection</h2>"
    if ($currentConnection.IsHotspot) {
        $htmlReport += "<div class='warning'><strong>⚠️ CONNECTED TO HOTSPOT: $($currentConnection.SSID)</strong><br>"
        $htmlReport += "BSSID: $($currentConnection.BSSID)<br>"
        $htmlReport += "Channel: $($currentConnection.Channel) | Signal: $($currentConnection.Signal)<br><br>"
        $htmlReport += "<strong>Hotspot Indicators:</strong><ul>"
        foreach ($indicator in $currentConnection.HotspotIndicators) {
            $htmlReport += "<li>$indicator</li>"
        }
        $htmlReport += "</ul></div>"
    } else {
        $htmlReport += "<div class='info'>Connected to: <strong>$($currentConnection.SSID)</strong><br>"
        $htmlReport += "BSSID: $($currentConnection.BSSID) | Channel: $($currentConnection.Channel) | Signal: $($currentConnection.Signal)</div>"
    }
}

# Hosted network
$htmlReport += "<h2>Hosted Network Status</h2>"
if ($hostedNetworkActive) {
    $htmlReport += "<div class='warning'>ACTIVE - SSID: $hostedNetworkSSID, Clients: $hostedNetworkClients</div>"
} else {
    $htmlReport += "<div class='success'>Inactive</div>"
}

# Mobile hotspot
$htmlReport += "<h2>Mobile Hotspot Service</h2>"
if ($mobileHotspotActive) {
    $htmlReport += "<div class='warning'>RUNNING</div>"
} else {
    $htmlReport += "<div class='success'>Stopped</div>"
}

# Network profiles
$htmlReport += "<h2>Network Profiles</h2><table><tr><th>SSID</th><th>Type</th></tr>"
foreach ($profile in $networkProfiles) {
    $rowClass = if ($profile.IsHotspot) { " class='hotspot-row'" } else { "" }
    $type = if ($profile.IsHotspot) { "HOTSPOT" } else { "WiFi" }
    $htmlReport += "<tr$rowClass><td>$($profile.SSID)</td><td>$type</td></tr>"
}
$htmlReport += "</table>"

# Virtual adapters
if ($virtualAdapters.Count -gt 0) {
    $htmlReport += "<h2>Virtual Adapters</h2><table><tr><th>Description</th><th>MAC</th></tr>"
    foreach ($adapter in $virtualAdapters) {
        $htmlReport += "<tr><td>$($adapter.Description)</td><td>$($adapter.MAC)</td></tr>"
    }
    $htmlReport += "</table>"
}

$htmlReport += "<h2>Connected Devices</h2><table><tr><th>IP Address</th><th>MAC Address</th></tr>"
foreach ($device in $connectedDevices) {
    $htmlReport += "<tr><td>$($device.IP)</td><td>$($device.MAC)</td></tr>"
}
$htmlReport += "</table>"

$htmlReport += "</body></html>"

try {
    $htmlReport | Out-File -FilePath $OutputPath -Encoding UTF8 -Force
    Write-Host "  Report saved successfully" -ForegroundColor Green
} catch {
    Write-Host "  Error saving report: $_" -ForegroundColor Red
}

Write-Host ""
Write-Host "SUMMARY:" -ForegroundColor Cyan
Write-Host "  Suspicious Activities: $($suspiciousActivities.Count)" -ForegroundColor $(if ($suspiciousActivities.Count -gt 0) { "Red" } else { "Green" })
Write-Host "  Hotspot Profiles: $(($networkProfiles | Where-Object { $_.IsHotspot }).Count)" -ForegroundColor $(if (($networkProfiles | Where-Object { $_.IsHotspot }).Count -gt 0) { "Yellow" } else { "Green" })
Write-Host "  Hosted Network: $(if ($hostedNetworkActive) { 'ACTIVE' } else { 'Inactive' })" -ForegroundColor $(if ($hostedNetworkActive) { "Red" } else { "Green" })
Write-Host "  Mobile Hotspot: $(if ($mobileHotspotActive) { 'RUNNING' } else { 'Stopped' })" -ForegroundColor $(if ($mobileHotspotActive) { "Red" } else { "Green" })
Write-Host "  Virtual Adapters: $($virtualAdapters.Count)" -ForegroundColor $(if ($virtualAdapters.Count -gt 0) { "Yellow" } else { "Green" })
Write-Host "  Connected Devices: $($connectedDevices.Count)" -ForegroundColor Cyan

if ($suspiciousActivities.Count -gt 0) {
    Write-Host "`nWARNINGS:" -ForegroundColor Red
    foreach ($activity in $suspiciousActivities) {
        Write-Host "  - $activity" -ForegroundColor Yellow
    }
}

Write-Host "`nReport Location: $OutputPath" -ForegroundColor Cyan
Write-Host "`nOpening report in browser..." -ForegroundColor Yellow

try {
    Start-Process $OutputPath
} catch {
    Write-Host "Could not open browser automatically. Please open the report manually." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Hit up @praiselily on dsc if you run into any issues" -ForegroundColor Cyan
Write-Host ""
Write-Host "Script complete. Press any key to exit..." -ForegroundColor Green
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
