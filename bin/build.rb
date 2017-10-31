#!/usr/bin/env ruby

PROJECT_ROOT = File.expand_path(File.dirname(__FILE__) + '/..')
FREIGHT_TARGET = 'packages@packages'

require 'fileutils'
require 'tmpdir'
require 'yaml'
require 'pathname'
require 'benchmark'
require PROJECT_ROOT + '/bin/trollop'

def check_for_dpkg
  dpkg_installed = system 'which dpkg > /dev/null'
  unless dpkg_installed
    puts 'Package \'dpkg\' is required to verify debs. OSX users, try \'brew install dpkg\'. Exiting....'
    exit(1)
  end
end

def remove_old_debian_package(package_name)
  FileUtils.rm_rf(Dir.glob("#{PROJECT_ROOT}/artefacts/#{package_name}_*_amd64.deb"))
end

def debian_package_filepath(build_number, package_name)
  package_filename = debian_package_filename(build_number, package_name)
  File.expand_path("#{PROJECT_ROOT}/artefacts/#{package_filename}")
end

def debian_package_filename(build_number, package_name)
  "#{package_name}_#{build_number}_amd64.deb"
end

def build_debian_package(build_number, service_path, service_name,  package_name)
  Dir.chdir("#{PROJECT_ROOT}/#{service_path}/build/output")
  FileUtils.rm_rf('deb/')
  FileUtils.mkdir_p("deb/ida/#{package_name}")
  FileUtils.mkdir_p("deb/opt/orch/#{package_name}")
  FileUtils.mkdir_p("deb/var/log/ida/debug")
  FileUtils.mkdir_p("deb/etc/logrotate.d")
  FileUtils.cp("#{service_name}.jar", "deb/ida/#{package_name}/#{package_name}.jar")
  FileUtils.cp("#{PROJECT_ROOT}/debian/#{package_name}/orch-deploy", "deb/opt/orch/#{package_name}/deploy")
  FileUtils.cp("#{PROJECT_ROOT}/debian/#{package_name}/orch-ready", "deb/opt/orch/#{package_name}/ready")
  FileUtils.cp("#{PROJECT_ROOT}/debian/#{package_name}/orch-restart", "deb/opt/orch/#{package_name}/restart")
  FileUtils.cp("#{PROJECT_ROOT}/debian/#{package_name}/logrotate-console-log", "deb/etc/logrotate.d/#{package_name}")
  FileUtils.cp_r('lib', "deb/ida/#{package_name}/")
  FileUtils.cp("#{PROJECT_ROOT}/configuration/#{package_name}.yml", "deb/ida/#{package_name}/#{package_name}.yml")

  package_file_name = debian_package_filepath(build_number, package_name)
  system "bundle exec fpm -C deb -s dir -t deb -n '#{package_name}' -v #{build_number} --deb-no-default-config-files --deb-upstart #{PROJECT_ROOT}/debian/#{package_name}/upstart/#{package_name} --prefix=/ --after-install #{PROJECT_ROOT}/debian/#{package_name}/postinst.sh -p #{package_file_name} ."

  unless $? == 0
    raise 'fpm encountered an error'
  end
end

def verify_debian_package(package_file_path, package_identifier, install_prefix)
  puts "looking for: #{install_prefix}/#{package_identifier}/#{package_identifier}.jar"
  jar_exists = system %Q(dpkg -c #{package_file_path} | grep '#{install_prefix}/#{package_identifier}/#{package_identifier}\.jar$' > /dev/null)
  unless jar_exists
    raise 'Invalid debian package structure.'
  end
end

def ensure_artefacts_directory_exists
  Dir.chdir(PROJECT_ROOT)
  FileUtils.mkdir_p('artefacts')
end

opts = Trollop::options do
  opt :upload_to_goverac28, 'Upload the generated packages to goverac28 (apt-ftparchive)'
  opt :upload_to_packages, 'Upload the generated packages to packages-1 (freight)'
end

check_for_dpkg

system('bundle install')

puts('Task took: ' + Benchmark.realtime do
    service_map = YAML::load_file("#{PROJECT_ROOT}/bin/services.yaml")
    service_map.each do |package_name, attributes|
      service_path = attributes['path']
      puts "Building debian package: #{package_name}"
      service_name = service_path.split('/')[1]
      build_number = ENV.fetch('BUILD_NUMBER', '0')
      ensure_artefacts_directory_exists
      install_prefix = '/ida'
      remove_old_debian_package(package_name)
      build_debian_package(build_number, service_path, service_name, package_name)
      package_filepath = debian_package_filepath(build_number, package_name)
      verify_debian_package(package_filepath, package_name, install_prefix)
      package_filename = Pathname.new(package_filepath).basename

      if opts[:upload_to_packages]
        puts "Uploading debian package to packages: #{package_name}"

        system "scp #{package_filepath} #{FREIGHT_TARGET}:/tmp"

        commands = [
                    "freight add /tmp/#{package_filename} apt/precise/main",
                    "rm /tmp/#{package_filename}"
                   ].join(' && ')
        puts commands
        system "ssh #{FREIGHT_TARGET} '#{commands}'"
      end
    end
end.to_s + ' seconds.')
