# Thai ID OCR Scanner

## Description
Thai ID OCR Scanner is a mobile application designed to analyze Thai ID cards using Optical Character Recognition (OCR). The app integrates with Google Vision API for OCR processing, extracting key information such as name, last name, identification number, date of birth, date of issue, and date of expiry. The extracted data is then structured and saved in a database in JSON format.

## Objective
The main objectives of the Thai ID OCR Scanner are:

- Develop an application that uses OCR to analyze Thai ID cards and extract relevant data.
- Integrate with Google Vision API for OCR processing.
- Parse the OCR results and interpret them, returning the final data in JSON format.
- Utilize a chosen database for CRUD operations on OCR data.

## Sample Thai ID Card Data
The ID cards may contain the following fields:

- Name
- Last Name
- Identification Number
- Date of Issue
- Date of Expiry
- Date of Birth

Sample THAI id card https://pbs.twimg.com/media/FkcR718VEAAMEtL.jpg:large

## OCR Processing
OCR stands for Optical Character Recognition. It is the procedure that transforms a text image into a text format.

## Data Extraction and Structuring
Build up logics to extract only useful information from the OCR processing. If all the data are not present then showing the user an error message.

## User Interface
- Upload a Thai ID card image in PNG, JPEG, or JPG format
- Limit file size to 2MB
- Displaying the JSON output for the OCR result
- Database and Crud Operations options
- Switch between themes

## Database Management and Rest API endpoints
- Add OCR Data Manually
- Update Existing OCR Data
- Perform search operations
- Sorting options also available
- Delete OCR Records

## JSON Output
The final data saved in the database is in JSON format.

## Technologies Used
- Mobile Vision API
- ML-Kit Text Recognition libraries
- Firebase
- Android Studio
- Java

## Dependencies
- implementation 'com.google.android.gms:play-services-vision:20.1.3'
- implementation 'com.google.mlkit:text-recognition:16.0.0'
- implementation 'com.google.android.gms:play-services-mlkit-text-recognition:19.0.0'
- implementation 'com.google.firebase:firebase-database:20.2.2'

## Installation
Download the APK source file and install it on your Android device.

## Contributing
Solo Levelling -> Ritik Mor

## Acknowledgments
- Google Vision ML Kit
- Mobile Vision ML Kit
- Firebase
- Qoala Placement Assignment

## Author
- Ritik Mor (https://github.com/RtkMor)
