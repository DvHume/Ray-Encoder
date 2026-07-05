> [!NOTE]
> 
> Этот проект своего рода тренировка.
 
![Ray](justray.svg)
# 🔐 QUARK Ray Encoder

![RayEncoder](https://img.shields.io/badge/RayEncoder-Project-orange)
[![MIT License](https://img.shields.io/badge/license-MIT-green)](LICENSE.txt)
[![Java 21](https://img.shields.io/badge/Java-21-orange)](https://adoptium.net/temurin/releases?version=21&os=any&arch=any)

Менеджер зашифрованных данных с консольным интерфейсом.
Проект построен на базе гибридного стека: криптографическое ядро написано на **Rust.**
А пользовательский интерфейс на **Java**

> Ray Encoder шифрует пользовательские данные с помощью аутентифицированного шифрования(AES-GCM).
>
> Ключ шифрования получается из пароля пользователя с помощью Argon2id,\
> что делает атаки методом перебора значительно дорогостоящими

---
## Как это работает?

### 🔐 Зашифровать пароль

1. Выберите **Encrypt data**
2. Введите пароль, который хотите защитить.
3. Введите **мастер-ключ**
4. Программа сгенерирует и выведет зашифрованную строку.
   - Сохраните строку - она понадобится для расшифровки(а так же не забудьте свой мастер-ключ)

### 🔓 Расшифровать данные

1. Выберите **Decrypt data**
2. Вставьте ранее полученную **зашифрованную строку**.
3. Введите тот же **мастер-ключ**, который использовался при шифровании.
4. Программа восстановит и отобразит исходный пароль
---

---

## 🛠 Стек:
* **Core** Rust (crates: `aes-gcm`, `argon2`, `rand`, `base64`)
* **Java 21** (***JNA*** для связи с нативным кодом)

---

## 🚀 Как запустить

Если вы скачали готовый релиз, вам не нужно ничего настраивать.
Внутри архива уже находятся скомпилированные библиотеки под вашу ОС

1. Убедитесь что у вас установлена **Java 21** (или выше).
2. Разархивируйте и запустите:
```bash
# Windows
run.bat

# Linux / macOS
chmod +x run.sh
./run.sh
```

## 🏗 Как собрать проект из исходников

Если вы хотите клонировать репозиторий и собрать проект самостоятельно, следуйте этой инструкции.

> [!WARNING]
> 
> Важная памятка по крипто-ядру (`.dll` / `.so` / `.dylib`)

Чтобы Java смогла общаться с кодом Rust, ей необходим файл динамической библиотеки,
скомпилированный под вашу ОС. Вы можете получить его двумя способами: 

1. Способ(без установки Rust):
Просто перейдите во вкладку [**GitHub Actions**](https://github.com/DvHume/Ray-Encoder/actions) в этом репозитории,
выберите последний успешный запуск **Workflow** и скачайте архив(build artifacts) для вашей ОС.
2. Локальная сборка (Требуется установленный Rust):
Перейдите в папку ядра и соберите его вручную:

```bash
cd rust-core
cargo build --release
```

> [!IMPORTANT]
>
> Сборка под macOS на workflow происходит слишком долго, из-за загруженности и отсутствия свободнных раннеров.
> И понимая всё неудобство. Если у вас macOS, то пока что вам самим нужно будет собирать библиотеку по способу выше. 

## Куда положить готовую библиотеку?

Перед сборкой Java поместите полученный файл в папку ресурсов Java (`src/main/resources/`)
в соответствующую директорию:

|OS                 | File
| ----------------- | --------------------------------------------------- |
| Windows(64-bit):  | ``win32-x86-64/quark_password_encryptor.dll``       |
| Linux(64-bit):    | ``linux-x86-64/libquark_password_encryptor.so``     |
| macOS(Intel):     | ``darwin-x86-64/libquark_password_encryptor.dylib`` |

## Сборка и запуск

После переноса библиотеки соберите проект с помощью Maven:

```bash
cd java-core
mvn clean package
java -jar target/RayEncoder-*.jar
```

## Security notice

This project is intended for educational purposes.

It has not been audited and should not be used for production-grade security applications.

### Полезные ссылки:

[Java 21](https://adoptium.net/temurin/releases?version=21&os=any&arch=any)\
[Rust](https://rust-lang.org/learn/get-started/)\
[What is AES-256?](https://en.wikipedia.org/wiki/Advanced_Encryption_Standard)