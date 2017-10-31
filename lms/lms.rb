#!/usr/bin/env ruby
require 'sinatra'
require 'webrick'

set :server, 'webrick'
set :port, 50130

post '/test-rp/matching-service/POST', :provides => 'application/json' do
  '{ "result": "match" }' # or no-match
end

post '/test-rp/unknown-user/POST', :provides => 'application/json' do
  '{ "result": "success" }' # or failure
end
