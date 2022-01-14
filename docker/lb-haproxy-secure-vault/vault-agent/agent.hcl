exit_after_auth = false
pid_file        = "./pidfile"

vault {
  address = "http://vault-dev:8200"
}

auto_auth {
  method "approle" {
    config = {
      role_id_file_path   = "/etc/vault/roleid"
      secret_id_file_path = "/etc/vault/secretid"
    }

  }

  sink "file" {
    config = {
      path = "/vault-agent/token"
    }
  }
}

template {
  source      = "/vault-agent/node_cert.tmpl"
  destination = "/vault-agent/node.crt"
  perms       = 0700
}

template {
  source      = "/vault-agent/node_key.tmpl"
  destination = "/vault-agent/node.key"
  perms       = 0700
}

template {
  source      = "/vault-agent/ca_cert.tmpl"
  destination = "/vault-agent/ca.crt"
  perms       = 0700
}

template {
  source      = "/vault-agent/user_cert.tmpl"
  destination = "/vault-agent/client.root.crt"
  perms       = 0700
}

template {
  source      = "/vault-agent/user_key.tmpl"
  destination = "/vault-agent/client.root.key"
  perms       = 0700
}

template {
  source      = "/vault-agent/ui_cert.tmpl"
  destination = "/vault-agent/ui.crt"
  perms       = 0700
}

template {
  source      = "/vault-agent/ui_key.tmpl"
  destination = "/vault-agent/ui.key"
  perms       = 0700
}