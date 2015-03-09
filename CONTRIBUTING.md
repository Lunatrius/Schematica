## Contributing
### Submitting a Pull Request (PR)
So you found a bug in the code? Think you can make it more efficient? Want to help in general? Great!

1. If you haven't already, create a [GitHub account](https://github.com/signup/free).
2. Click the `Fork` icon located at the top-right of this page (below your username).
3. Make the changes that you want to and commit them.
    * If you're making changes locally, you'll have to do `git add -A`, `git commit` and `git push` in your command line.
4. Click `Pull Request` at the right-hand side of the gray bar directly below your fork's name.
5. Click `Click to create a pull request for this comparison`, enter your pull request title, and create a detailed description explaining what you changed.
6. Click `Send pull request`, and wait for feedback!

#### Instructions/Advice when submitting a Pull Request (PR)
I use [this](https://gist.github.com/460be5119b93d73c24ab) IDEA formatting. To be short:

* spaces for indentation
* braces on same lines

When you finish up your PR you'll want to [squash](http://davidwalsh.name/squash-commits-git) them into a single commit (unless it makes sense to have them split).

1. Make sure your working directory is clean by executing `git status`.
2. Execute `git rebase -i HEAD~X` where `X` is the amount of your commits. This will make sure you squash only your own commits.
3. You should now see a list of all your commits, prefixed with `pick`. Change all instances of `pick` (excluding the first!) into `squash` or simply `s`. Then save/quit the editor once.
4. A second screen should show up, displaying all the commit messages (you may edit them, delete or add some). After your done save/quit the editor again.
5. If git successfully rebased things simply push your cleaned up commits by executing `git push -f`.

#### Localization
You should always use `en_US.lang` as the base for localization to other languages. To modify the files you have two options:

* Create or modify the file directly with your favorite text editor (make sure encoding is set to `UTF-8`!).
* Use [this](http://mc.lunatri.us/translate) website to load the base file (and optionally the target language if you're updating localizations).
_Note: you **must** use the link to the `Raw` file._

### Creating an Issue
Crashing? Have a suggestion? Found a bug? Create an issue now!

1. Make sure your issue hasn't already been answered or fixed. Also think about whether your issue is a valid one before submitting it.
2. Click `New Issue` right below `Star` and `Fork`.
3. Enter your issue title (something that summarizes your issue), and then add a detailed description ("Hey, could you add/change stuff?" or "Hey, found an exploit: stuff").
    * If you are reporting a bug, make sure you include the following:
        * The log file `fml-client-latest.log` (or `fml-server-latest.log`) uploaded to [gist](https://gist.github.com/).
        * Detailed description of the bug.
            * Include steps on how to reproduce the bug (if possible).
4. Click `Submit new issue`, and wait for feedback!
