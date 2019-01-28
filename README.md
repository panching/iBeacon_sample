# iBeacon_sample


### 說明
一個簡單的藍牙連接搜尋iBeacon範例.

### 環境
<ul>
 <li>Android 3.3 version
 <li>一隻 Samsung Android 5手機
 <li>三個 Estimote iBeacons
 <li>compileSdkVersion 28
 <li>minSdkVersion 18
 <li>targetSdkVersion 28
</ul>

### 流程
1. 點擊螢幕後開始掃描環境並且做權限驗證<br/>
2. 顯示BEACON訊息＆距離當距離小於1時發送LocalNotification推播


### 備註
從Android 3.2使用com.android.support:appcompat-v7依賴套件升級到開發環境使用Android 3.3時若遭遇IDE相容性問題請將gradle.properties加上

<ul>
  <li>android.useAndroidX=true
  <li>android.enableJetifier=true
</ul>

即可使用AndroidX&之前的開發相關可不用Migrate
