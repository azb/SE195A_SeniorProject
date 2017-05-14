# SE195A_SeniorProject
IROM Prototype

Authors: Arthur Baney, Jack Dubil, Charles Oliver, Krystle Weinrich

Date: May 14, 2017

Github: https://github.com/azb/SE195A_SeniorProject

Demonstration Video: https://www.youtube.com/watch?v=FVovLsx2wrk

Install Requirements

1. Android Mobile Device with minimum OS Android 4.4 (KitKat)
    *Some features require usage of the devices camera and will not work on emulators.

2. IROM APK file (included with this submission)
    *Developer mode must be enabled to install the APK on your device. Consult your device’s manual for instructions how to enable developer mode on your device. Additionally, you must enable installation of apps from sources other than the Google Play store.


Install Instructions


1. Enable Developer Mode on your device. 
    *A common method to do this is to go to Settings>About Device and press “Build Number” five times. If you are successful, a toast message will appear notifying developer mode has been enabled. 


2. Enable installation of apps from sources other than the Google Play store.
    *This option is available in most devices under Settings>Lock Screen and security>Unknown Sources.


3. Copy the IROM APK onto your device’s storage.


4. Use a file browser application to locate the IROM APK and install it.


Usage Instructions

1. First time users will be greeted with the sign-up screen. Users need an e-mail to sign up. 

2. After logging in, the users are directed to the marketplace, where they can purchase an item on a sales listing from other users. To purchase from a sales listing, simply click the sales listing, and tap the purchase button. Currently, the application is not set up to make anything but "test" credit card charges, so use the card number 4242 4242 4242 4242 if you want to make any purchase tests; the expiration date and CVC can be anything. 

3. The user has an inventory that contains "items". To add an item to the inventory, click the "add item" button in the navigation drawer. Here, the user must upload a picture of the item they want to add. To do so, click the camera image, and take a picture with the device camera, or upload from the device gallery. After that, the application will attempt to identify the contents of the image. It will do so by comparing the image with existing products in the product database. If no match is found, the user can enter their item's name, description and suggested price and submit it to the product database. If a match is found, the user can select the matching item listed. From there, the user can either add the item to their inventory for future use, or create a sales listing for the item then.

4. The user's inventory, the user can see what items they added, create sales listings from them, and manage sales listings. 


Code Structure
   Application code can be found in the IROM\app\src\main\java\com\sjsu\se195\irom folder. 
   Display resources can be found in the IROM\app\src\main\res folder.
   Stripe code used for Stripe payment libraries and resources can be found in the IROM\stripe\ folder.

Libraries Used:

   Firebase

   Google Cloud Vision

   PickImage - https://github.com/jrvansuita/PickImage

   Stripe

   Noodlio Pay - https://noodliopay.com/
   
   Android Asynchronous Http Client - http://loopj.com/android-async-http/


Vector Icon Credit:

   image-add-button_14232  - Icon made by Picol on www.flaticon.com
  
   user and user_large - Icons made by chanut-is-industries on www.flaticon.com
