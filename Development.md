# Development
To get started, here are all the tools you need:
* [GitHub Desktop](https://desktop.github.com/) or [Git](https://git-scm.com/)
* [IntelliJ IDEA](https://www.jetbrains.com/idea/) (Community Edition is fine)

## Cloning Repository with GitHub Desktop
1. Open this repository on GitHub and click **Code** > **Copy url to clipboard**.
![image](https://github.com/pk-hack/EbProjectEditor/assets/12485457/6238feeb-e279-4a1c-94b9-f7eb42c5dba7)
2. Open GitHub Desktop and choose "Clone a repository from the Internet...".
![image](https://github.com/pk-hack/EbProjectEditor/assets/12485457/e06fabb0-eb2c-4005-ad54-34156dc33b46)
3. Click the URL tab.
![image](https://github.com/pk-hack/EbProjectEditor/assets/12485457/38686a16-5902-49b4-a8f9-b2af09755416)
4. Paste the URL you copied into the upper text box.
![image](https://github.com/pk-hack/EbProjectEditor/assets/12485457/dc5752db-62c8-438e-bfe1-44dc9986d638)
5. Enter the directory where you would like to clone all the source files in the **Local Path** box and click **Clone**.
![image](https://github.com/pk-hack/EbProjectEditor/assets/12485457/78cfcc09-2786-4005-8118-2f37f06a0176)
6. _If you want to build/run a version not yet merged to the main branch_, click **Current branch** and select the relevant branch.
![image](https://github.com/pk-hack/EbProjectEditor/assets/12485457/4afdb600-4841-4cf1-bef2-aa4a4134ae71)

## Cloning Repository with Git
1. Open this repository on GitHub and click **Code** > **Copy url to clipboard**.
![image](https://github.com/pk-hack/EbProjectEditor/assets/12485457/6238feeb-e279-4a1c-94b9-f7eb42c5dba7)
2. Using your preferred command line application, navigate to the directory where you would like the repository directory to be cloned.
3. Type `git clone` followed by a space and the URL you copied, then press enter.
![image](https://github.com/pk-hack/EbProjectEditor/assets/12485457/e095768d-a5ae-4c91-bd28-db68325c5af9)
4. _If you want to build/run a version not yet merged to the main branch_, navigate to the repository directory that was just created and type `git checkout --track remotes/origin/` followed by the name of the relevant branch, then press enter.
![image](https://github.com/pk-hack/EbProjectEditor/assets/12485457/5c6c4d1f-00e1-4c6b-8eda-c65e8a04c230)

## Building & Running
1. Open IntelliJ IDEA and click **Open**.
![image](https://github.com/pk-hack/EbProjectEditor/assets/12485457/2f052882-2f39-4756-b193-74f2371f479e)
2. Select the repository directory and click **OK**.
![image](https://github.com/pk-hack/EbProjectEditor/assets/12485457/b4a9a522-6b06-4288-8fd7-fda36ce5a061)
3. Click the **Run** button or press **Shift**+**F10**. An EB Project Editor window should appear.
![image](https://github.com/pk-hack/EbProjectEditor/assets/12485457/fe510c29-b714-4fc5-8b22-8e55bafa55c6)
4. When you're done, click the **Stop** button, press **Ctrl**+**F2**, or just close the EB Project Editor window.
![image](https://github.com/pk-hack/EbProjectEditor/assets/12485457/9f54f3c7-a230-40fe-9d43-9287d2a38358)

Running the project in IntelliJ IDEA should also generate a distributable JAR file in the repository directory's **target/** subdirectory.
![image](https://github.com/pk-hack/EbProjectEditor/assets/12485457/10fd4b3a-f305-4b31-b2a9-a95262336ed0)

