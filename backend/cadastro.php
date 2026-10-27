<?php
header('Content-Type: application/json');
$conn = new PDO("mysql:host=localhost;port=3307;dbname=db_unicarreiras;charset=utf8mb4", "root", "");

if (!isset($_POST['nome']) || !isset($_POST['email']) || !isset($_POST['senha']) ||
    !isset($_POST['curso']) || !isset($_POST['instituicao_ensino'])) {
    echo json_encode(["sucesso" => false, "mensagem" => "Dados incompletos"]);
    exit;
}

$nome = $_POST['nome'];
$email = $_POST['email'];
$senha = password_hash($_POST['senha'], PASSWORD_DEFAULT);
$curso = $_POST['curso'];
$instituicao = $_POST['instituicao_ensino'];

try {
    $stmt = $conn->prepare("INSERT INTO estudante (nome, email, senha, curso, instituicao_ensino) VALUES (?, ?, ?, ?, ?)");
    $stmt->execute([$nome, $email, $senha, $curso, $instituicao]);
    echo json_encode(["sucesso" => true, "mensagem" => "Conta criada com sucesso!"]);
} catch (PDOException $e) {
    echo json_encode(["sucesso" => false, "mensagem" => "E-mail já cadastrado"]);
}