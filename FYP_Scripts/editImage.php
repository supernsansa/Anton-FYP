<?php
//Server and DB connection values
$servername = "localhost";
$rootUser = "root";
$db = "bsl4kids";
$rootPassword = "";

//Create connection
$conn = new mysqli($servername, $rootUser, $rootPassword, $db);

//Check connection
if ($conn->connect_error)
	{
		die("Connection failed: " . $conn->connect_error);
	}

$word_name = $_POST["wordName"];
$image_name = $_POST["fileName"];

$imageQuery = "UPDATE worddb SET ImageFilename = '$image_name' WHERE WordName = '$word_name'";
$conn->query($imageQuery);

//Test if query is successful
if ($conn->query($imageQuery) == TRUE) {
	echo "Success, your image has been set!";
}
else {
	echo "Some error has occurred";
}
$conn->close();
?>