<?php
header('Content-Type: application/json');
$conn = new PDO("mysql:host=localhost;port=3307;dbname=db_unicarreiras;charset=utf8mb4", "root", "");

if (!isset($_POST['email']) || !isset($_POST['senha'])) {
    echo json_encode(["sucesso" => false, "mensagem" => "Dados incompletos"]);
    exit;
}

$email = $_POST['email'];
$senha = $_POST['senha'];

$stmt = $conn->prepare("SELECT * FROM estudante WHERE email = ?");
$stmt->execute([$email]);
$user = $stmt->fetch(PDO::FETCH_ASSOC);

if ($user && password_verify($senha, $user['senha'])) {
    unset($user['senha']);
    echo json_encode(["sucesso" => true, "mensagem" => "Login realizado com sucesso!", "usuario" => $user]);
} else {
    echo json_encode(["sucesso" => false, "mensagem" => "E-mail ou senha incorretos"]);
}