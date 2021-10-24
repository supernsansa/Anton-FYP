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
$tag_name = $_POST["tagName"];

//First, probe db to check if tag already exists
$myArray = array();
if ($result = $conn->query("SELECT * FROM tagdb WHERE Word = '$word_name' AND TagName = '$tag_name'")) {
	if (mysqli_num_rows($result) > 0) {
		echo 'This tag already exists';
	}
	else {
		//Query to worddb
		$tagQuery = "INSERT INTO tagdb (TagName, Word) Values('$tag_name','$word_name')";
		//Test if query is successful
		if ($conn->query($tagQuery) == TRUE) {
			echo "Tag Added Successfully";
		}
		else {
			echo "Some error has occurred (inner)";
		}
	}
}
else {
	echo 'Some error has occured';
}
$conn->close();
?>