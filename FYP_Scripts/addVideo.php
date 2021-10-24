<?php
//This script edits the definition of a word
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

//Get word and word description from app
$word_name = $_POST["wordName"];
$file_name = $_POST["fileName"];
$user = $_POST["userName"];

//Query to videodb
$videoQuery = "INSERT INTO videodb (Word, FileName, User) Values('$word_name','$file_name','$user')";
//Test if query is successful
if ($conn->query($videoQuery) == TRUE)
{
  echo "Video entry created";
}
else
{
  echo "Failure: For some reason";
}
$conn->close();
?>