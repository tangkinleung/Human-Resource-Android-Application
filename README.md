# csc2007-team10-2022
2007 Android HR Application

### Advanced Features
    Android JetPack CameraX: 
      ImageAnalysis:
        Used for QR Code Scanning Activity, specifically used to scan the QR Code by calling the ML Kit barcode scanner library for each frame during the QRCameraActivity.
        
    Google ML Kit: 
      ML Kit barcode scanner:
        Used to interpret information from a barcode and is used together with CameraX as part of the QR Code Scanning Activity.
        
    Google Firebase Service
      Authentication:	
        Used as part of login activity for user registration and login. Also provided unique user identification that helps sort the documents within Realtime Database.

      Storage:
        Used as part of claims applications and amendments in which it is used to store and retrieve uploaded images from users.
    
      Realtime Database:
        Used through the application for updated user documents and retrieving documents for display for specific activities.

### Third-party libraries & Services
    Google ML Kit library
    Google Firebase Services
