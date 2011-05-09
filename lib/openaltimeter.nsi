Name "openaltimeter"
OutFile "..\build\openaltimeter_win_${VERSION}.exe"
InstallDir $PROGRAMFILES\openaltimeter
Icon "..\lib\logo_short_128.ico"

SetCompressor lzma

; Registry key to check for directory (so if you install again, it will 
; overwrite the old one automatically)
InstallDirRegKey HKLM "Software\openaltimeter" "Install_Dir"

; Request application privileges for Windows Vista
RequestExecutionLevel admin

;--------------------------------

; Pages

Page components
;Page directory
Page instfiles

UninstPage uninstConfirm
UninstPage instfiles

;--------------------------------

; The stuff to install
Section "openaltimeter downloader (required)"

  SectionIn RO
  
  SetOutPath $INSTDIR
  
  File /r ..\build\windows\downloader\*.*
  
  ; Write the installation path into the registry
  WriteRegStr HKLM SOFTWARE\openaltimeter "Install_Dir" "$INSTDIR"
  
  ; Write the uninstall keys for Windows
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\openaltimeter" "DisplayName" "openaltimeter"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\openaltimeter" "UninstallString" '"$INSTDIR\uninstall.exe"'
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\openaltimeter" "DisplayIcon" '"$INSTDIR\logo_short_128.ico"'
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\openaltimeter" "Publisher" "http://openaltimeter.org"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\openaltimeter" "DisplayVersion" '${VERSION}'
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\openaltimeter" "EstimatedSize" 80000
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\openaltimeter" "NoModify" 1
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\openaltimeter" "NoRepair" 1
  WriteUninstaller "uninstall.exe"
  
SectionEnd

; Optional section (can be disabled by the user)
Section "Start Menu Shortcuts"

  CreateDirectory "$SMPROGRAMS\openaltimeter"
  CreateShortCut "$SMPROGRAMS\openaltimeter\Uninstall.lnk" "$INSTDIR\uninstall.exe" "" "$INSTDIR\uninstall.exe" 0
  CreateShortCut "$SMPROGRAMS\openaltimeter\openaltimeter downloader.lnk" "$INSTDIR\downloader.exe"
  
SectionEnd

;--------------------------------

; Uninstaller

Section "Uninstall"
  
  ; Remove registry keys
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\openaltimeter"
  DeleteRegKey HKLM SOFTWARE\openaltimeter

  ; Remove files and uninstaller
  RMDir /r $INSTDIR

  ; Remove shortcuts, if any
  Delete "$SMPROGRAMS\openaltimeter\*.*"

  ; Remove directories used
  RMDir "$SMPROGRAMS\openaltimeter"

SectionEnd
