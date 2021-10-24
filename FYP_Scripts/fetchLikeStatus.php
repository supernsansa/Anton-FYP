<?php
//This script probes the word databse to check if a word entry has already been made
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

//Get word from app
$word_name = $_POST["wordName"];
$username = $_POST["username"];

$myArray = array();
if ($result = $conn->query("SELECT * FROM likedb WHERE Word = '$word_name' AND User = '$username'")) {
	if (mysqli_num_rows($result) > 0) {
		echo 'true';
	}
	else {
		echo 'false';
	}
}
else {
	echo 'Some error has occured';
}

//$result->close();
$conn->close();
?>