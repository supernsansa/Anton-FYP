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

//Query to worddb
$likeQuery = "SELECT COUNT(*) AS total FROM likedb WHERE Word = '$word_name'";
//Test if query is successful
if ($result = $conn->query($likeQuery)) {
	$row = $result->fetch_array(MYSQLI_ASSOC);
	echo $row['total'];
}
else {
	echo "Some error has occurred";
}
$conn->close();
?>