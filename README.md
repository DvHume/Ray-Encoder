> [!NOTE]
> 
> Этот проект своего рода тренировка.
 
# 🔐 QUARK Ray Encoder

Менеджер зашифрованных данных с консольным интерфейсом(пока что).
Проект построен на базе гибридного стека: криптографическое ядро написано на **Rust.**
А пользовательский интерфейс на **Java**

---

## 🛠 Стек:
* **Core** Rust (crates: `aes-gcm`, `argon2id`, `rand`, `base64`)
* **Java 21** (***JNA*** для связи с нативным кодом)

---

## 🚀 Как запустить

Если вы скачали готовый релиз, вам не нужно ничего настраивать.
Внутри архива уже находятся скомпилированные библиотеки под вашу ОС

1. Убедитесь что у вас установлена **Java 21** (или выше).
2. Запустите приложение из zip-архива:\
```txt
run.bat
run.sh
```

## 🏗 Как собрать проект из исходников

Если вы хотите клонировать репозиторий и собрать проект самостоятельно, следуйте этой инструкции.

> [!WARNING]
> 
> Важная памятка по крипто-ядру (`.dll` / `.so` / `.dylib`)

Чтобы Java смогла общаться с кодом Rust, ей необходим файл динамической библиотеки,
скомпилированный под вашу ОС. Вы можете получить его двумя способами: 

1. Способ(без установки Rust):
Просто перейдите во вкладку [**Actions**](https://github.com/DvHume/Ray-Encoder/actions) в этом репозитории,
выберите последний успешный запуск **Workflow** и скачайте архив(Artifacts) для вашей ОС.
2. Локальная сборка (Требуется установленный Rust):
Перейдите в папку ядра и соберите его вручную:

```bash
cd rust-core
cargo build --release
```

## Куда положить готовую библиотеку?

Перед сборкой Java поместите полученный файл в папку ресурсов Java (`src/main/resources/`)
в соответствующую директорию:

- Windows(64-bit): <span style="color: gray; pointer-events: none; cursor: default">win32-x86-64/quark_password_encryptor.dll</span>

- Linux(64-bit): <span style="color: gray; pointer-events: none; cursor: default">linux-x86-64/quark_password_encryptor.so</span>

- macOS(Intel): <span style="color: gray; pointer-events: none; cursor: default">darwin-x86-64/quark_password_encryptor.dylib</span>

## Сборка и запуск

После переноса библиотеки соберите проект с помощью Maven:

```bash
cd java-core
mvn clean package
java -jar target/RayEncryptor-*.jar
```

### Полезные ссылки:

[Java 21](https://adoptium.net/temurin/releases?version=21&os=any&arch=any)\
[Rust](https://rust-lang.org/learn/get-started/)\
[What is AES-256?](https://en.wikipedia.org/wiki/Advanced_Encryption_Standard)