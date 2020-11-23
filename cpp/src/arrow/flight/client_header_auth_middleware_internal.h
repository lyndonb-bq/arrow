// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

// Interfaces for defining middleware for Flight clients. Currently
// experimental.

#pragma once

#include "arrow/flight/client_middleware.h"

#ifdef GRPCPP_PP_INCLUDE
#include <grpcpp/grpcpp.h>
#if defined(GRPC_NAMESPACE_FOR_TLS_CREDENTIALS_OPTIONS)
#include <grpcpp/security/tls_credentials_options.h>
#endif
#else
#include <grpc++/grpc++.h>
#endif

namespace arrow {
namespace flight {
namespace internal {

/// \brief Add basic authentication header key value pair to context.
///
/// \param context grpc context variable to add header to.
/// \param username username to encode into header.
/// \param password password to to encode into header.
void ARROW_FLIGHT_EXPORT AddBasicAuthHeaders(grpc::ClientContext* context,
                                             const std::string& username,
                                             const std::string& password);

/// \brief Client-side middleware for receiving and latching a bearer token.
class ARROW_FLIGHT_EXPORT ClientBearerTokenFactory : public ClientMiddlewareFactory {
 public:
  /// \brief Constructor for factory.
  ///
  /// \param[out] bearer_token_ pointer to a std::pair of std::strings that the factory
  ///     will populate with the bearer token that is received from the server.
  explicit ClientBearerTokenFactory(std::pair<std::string, std::string>* bearer_token_);

  ~ClientBearerTokenFactory();

  void StartCall(const CallInfo& info, std::unique_ptr<ClientMiddleware>* middleware);

 private:
  class Impl;
  std::unique_ptr<Impl> impl_;
};

}  // namespace internal
}  // namespace flight
}  // namespace arrow
