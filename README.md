# gradle-kdiff

Gradle plugin that helps to diff Kubernetes manifests that are managed and built with [Kustomize](https://kustomize.io/).

## Usage

Apply the plugin to your Kustomize project in the `build.gradle.kts` file:

```kotlin
plugins {
    id("at.mibe.gradle.kdiff") version "0.1.0"
}
```

The plugin adds a group of tasks `kdiff` to your project:

- `kDiffVersion`: Prints the version of the plugin and the `kdiff` command line tool.

- `installKustomize`: Installs the `kustomize` command line tool. Required by `kdiff` to work.

> **Note:** This task is only supported on Linux and MacOS. If you want to use it on Windows, you have to download and
> install `kustomize` manually for example by using the [Chocolatey](https://kubectl.docs.kubernetes.io/installation/kustomize/chocolatey/)
> package manager.

- `installKDiff`: Installs the `kdiff` command line tool. This is a wrapper around `kustomize` that adds the ability to
  diff manifests between different branches.

> **Note:** By default, `kdiff` will use the `kustomize` executable that is installed next to the `kdiff` executable.
> If you want to use a different `kustomize` executable, you can set it as the `--kustomize` argument in `kdiff`.

The `kdiff` CLI will be installed into the `build/bin` directory of your project. You can add this directory to your
`.gitignore` file in order to avoid committing the CLI to your repository.

```shell
./build/bin/kdiff <path-to-kustomize-file> -b <remote-branch>
```

Example:

```shell
./build/bin/kdiff k8s/ -b origin/master
```

`kdiff` will then create a temporary directory and clone the remote branch into it. It will then run `kustomize build`
on both the local and the remote branch and diff the resulting manifests.

## Known Limitations

- Currently, only diffs against remote (pushed) branches are supported. Diffs against local branches are not supported
  yet.
