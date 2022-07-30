# ECE 531 Final ARM code

Small C program that implements get, post, delete and put HTTP requests via curl api.
Code uses argp api.
Code comes with two make files, one for standard gcc, the other for cross compiling.
Run the following for linux based distro such as ubuntu:

```
make -f make-gcc
```

To run on ARM, make sure you have the proper packages and libraries installed such as curl and argp:

```
make -f make-arm
```

Finallly you can clean the project by running either:

```
make clean if make-arm or make clean -f make-gcc
```

Code was developed as a HW assignment to work along with the thermocouple submodule from cclamb.

It also interacts with a web server, code is also found in this repository.





