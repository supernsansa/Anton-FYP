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
$username = $_POST["username"];

//Query to worddb
$likeQuery = "INSERT INTO likedb (Word, User) Values('$word_name','$username')";
//Test if query is successful
if ($conn->query($likeQuery) == TRUE) {
	echo "Success";
	$likeQuery = "UPDATE worddb SET Likes = ((SELECT Likes FROM worddb WHERE WordName = '$word_name')+1) WHERE WordName = '$word_name'";
	$conn->query($likeQuery);
}
else {
	echo "Some error has occurred";
}
$conn->close();
?>