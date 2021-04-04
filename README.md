# **KidzDrawing**

_KidzDrawing_ is an Android based application developed using **Kotlin**. <br>
Each doodle is saved as a path in a static list. Each path is associated with a unique brush size and color. When invalidated, all the paths in the list are drawn onto the canvas using it's own properties.
<br>
<br>

![KidzDrawing_Intro](https://media4.giphy.com/media/Qyu735X5ojUpieqdvD/giphy.gif)

<br>
This application has an inbuilt color picker developed from scratch. The user can either manually stretch the seekbars to adjust the RGB values or provide a custom HEX value to select the color. It will be previewed onto the dialog for improved UX. <br>
Similarly the user can also select the brush size from another seekbar based dialog. <br>
<br>
<br>

![KidzDrawing_Undo_Redo](https://media1.giphy.com/media/fxqba6G8MeHahoQGlY/giphy.gif)

<br>
The application also provides an undo and redo feature. Undo will pop the path object from main list and push it to a secondary list. Redo will just reverse the same.<br>
The clear all funcationaliy will just clear the main list and invalidate the view.
<br>
<br>

![KidzDrawing_File_Import](https://media1.giphy.com/media/Oli5WFBnTb5jNkfw6Q/giphy.webp)

<br>
Users can also choose a background image to doodle/edit upon. The file import feature opens up an image picker. Once image is retrieved, it's alpha value is reduced to add a semi-transparent custom background to doodle upon.