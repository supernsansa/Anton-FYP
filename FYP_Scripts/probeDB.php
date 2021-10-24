<?php
//This script probes the word database to check if a word entry has already been made
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
$myArray = array();
if ($result = $conn->query("SELECT WordName FROM worddb WHERE WordName = '$word_name'")) {
	if (mysqli_num_rows($result) > 0) {
		echo 'exists';
	}
	else {
		echo 'proceed';
	}
}
else {
	echo 'Some error has occured';
}

//$result->close();
$conn->close();
?>